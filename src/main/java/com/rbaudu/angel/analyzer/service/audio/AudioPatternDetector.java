package com.rbaudu.angel.analyzer.service.audio;

import com.rbaudu.angel.analyzer.config.AnalyzerConfig;
import com.rbaudu.angel.analyzer.model.ActivityType;
import com.rbaudu.angel.analyzer.util.AudioUtils;
import com.rbaudu.angel.analyzer.util.ModelLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tensorflow.Result;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.types.TFloat32;

import jakarta.annotation.PostConstruct;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Service de détection de patterns audio pour l'identification d'activités.
 * 
 * Ce service s'appuie pour l'instant sur Yamnet et une correspondance est réalisée dans le fichier 'Angel/angel-server-capture/src/main/scripts/models/audioyamnet_class_map.xlsx'
 * Ce fichier est extrait du csv yamnet_class_map.csv qui liste les 521 sons détectés de Yamnet.
 * Le premier onglet de ce fichier correspond à cette extraction sans modification.
 * Le second onglet 'yamnet_class' trasnforme les données sur chaque colonne avec :
 * - Colonne A : Les categorie Angel d'activité associé à un index de son Yamnet. le format est "<Angel Category>[*<seuil de confiance pour prise en compte>]|<Angel Category>[*<seuil de confiance pour prise en compte>]|.... 
 *   Si '*<seuil de confiance pour prise en compte>' n'est pas présent, ce sera la valeur par défaut donné par le paramètre 'angel.analyzer.audioClassificationDefaultThreshold' de applicationProperties qui est prise en compte.
 * - Colonne B : l'index son de Yamnet qui sera retourné lors de l'analyse.
 * - Colonne C : la désignation en anglais
 * - Colonne F : la désignation en français
 * - Colonne H : les grandes catégories de son de Yamnet
 * - Colonne J : la parytie csv à copier dans le fichier csv donné par le paramètre 'angel.analyzer.audioClassificationMapping' ('models/audio_classification/correspondance_yamnet_index_and_angel_category.csv') de application.properties 
 *   et qui est utilisé par ce programme pour associé à un index son Yamnet détecté les catégories d'activité Yamnet à fournir à l'analyseur Angel.
 */
@Service
public class AudioPatternDetector {
    private static final Logger logger = LoggerFactory.getLogger(AudioPatternDetector.class);

    private final ModelLoader modelLoader;
    private final AudioUtils audioUtils;
    private final AnalyzerConfig config;
    private Hashtable<String, ThresholdSound[]> correspondanceAudioIndexToAngelCategory = new Hashtable<>();
    private double defaultThresholdSound = 0.5;
    private SavedModelBundle model;
    
    /**
     * Constructeur avec injection de dépendances.
     * @param modelLoader Chargeur de modèle TensorFlow
     * @param audioUtils Utilitaires audio
     * @param config Configuration de l'analyseur
     */
    public AudioPatternDetector(ModelLoader modelLoader, AudioUtils audioUtils, AnalyzerConfig config) {
        this.modelLoader = modelLoader;
        this.audioUtils = audioUtils;
        this.config = config;
        
        // Chargement du mapping des index Yamnet vers les catégories Angel
        loadSoundIndexMapping();
    }
    
    /**
     * Charge le mapping des index de sons vers les catégories Angel
     * depuis le fichier CSV configuré
     */
    private void loadSoundIndexMapping() {
        defaultThresholdSound = config.getAudioClassificationDefaultThreshold();
        String csvFile = config.getAudioClassificationMapping();
        String line;
        String csvSplitBy = ",";
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] keyValue = line.split(csvSplitBy);
                if (keyValue.length == 2) {
                    String soundIndex = keyValue[0];
                    String[] activityList = keyValue[1].split("\\|");
                    ThresholdSound[] thresholds = new ThresholdSound[activityList.length];
                    int i = 0;
                    for (String activity : activityList) {
                        String[] str = activity.split("\\*");
                        double thresholdValue = defaultThresholdSound;
                        if (str.length > 1) thresholdValue = Double.valueOf(str[1]);
                        String activityLabel = str[0];
                        ThresholdSound threshold = new ThresholdSound(activityLabel, thresholdValue);
                        thresholds[i++] = threshold;
                    }
                    correspondanceAudioIndexToAngelCategory.put(soundIndex, thresholds);               	
                }
            }
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du mapping des sons", e);
        }
    }
    
    /**
     * Initialisation du modèle après construction du bean.
     */
    @PostConstruct
    public void init() {
        if (!config.isAudioAnalysisEnabled()) {
            logger.info("Analyse audio désactivée dans la configuration");
            return;
        }
        
        try {
            if (config.getAudioClassificationModel() != null) {
                this.model = modelLoader.loadModel(config.getAudioClassificationModel());
                logger.info("Modèle de classification audio chargé avec succès");
            } else {
                logger.warn("Aucun modèle de classification audio configuré");
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du modèle de classification audio", e);
        }
    }
    
    /**
     * Détecte des patterns audio pour identifier les activités.
     * @param audioStream Flux audio à analyser
     * @return Map des types d'activités avec leur score de confiance
     */
    public Map<ActivityType, Double> detectAudioPatterns(AudioInputStream audioStream) {
        if (!config.isAudioAnalysisEnabled() || model == null) {
            logger.warn("Détection de patterns audio impossible : désactivée ou modèle non chargé");
            return new HashMap<>();
        }
        
        try {
            // Standardisation du format audio
            AudioFormat targetFormat = new AudioFormat(
                    config.getAudioSampleRate(),
                    16,    // bits par échantillon
                    1,     // mono
                    true,  // signé
                    false  // little endian
            );
            
            // Conversion du format si nécessaire
            AudioInputStream standardizedStream = audioUtils.convertAudioFormat(audioStream, targetFormat);
            
            // Extraction des caractéristiques MFCC
            byte[] audioData = new byte[(int) standardizedStream.getFrameLength() * targetFormat.getFrameSize()];
            standardizedStream.read(audioData);
            
            float[] mfcc = audioUtils.extractMFCC(audioData, targetFormat.getSampleRate(), 13);
            
            // Conversion en Tensor
            TFloat32 featureTensor = audioUtils.audioToTensor(mfcc);
            
            // Exécution de l'inférence
            Session.Runner runner = model.session().runner()
                    .feed("input", featureTensor)
                    .fetch("output");
            
            Result outputs = runner.run();
            TFloat32 resultTensor = (TFloat32) outputs.get(0);
            
            // Extraire les résultats du tensor (CORRECTION ICI)
            int numClasses = 5; // Supposons 5 types de sons identifiables
            float[] audioClasses = new float[numClasses];
            
            // Créer un FloatNdArray à partir du tableau pour recevoir les données
            FloatNdArray targetArray = NdArrays.ofFloats(audioClasses);
            
            // Copier les données du tensor vers notre tableau via le NdArray
            resultTensor.copyTo(targetArray);
            
            // Conversion en Map d'activités
            Map<ActivityType, Double> result = new HashMap<>();
            mapAudioClassesToActivities(audioClasses, result);
            
            logger.debug("Patterns audio détectés: {}", result);
            return result;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la détection de patterns audio", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Mappe les classes audio vers des types d'activités.
     * We use for that the 521 Yamnet audio classes :
     * index	mid	display_name
     * 0	/m/09x0r	Speech
     * 1	/m/0ytgt	Child speech, kid speaking
     * 2	/m/01h8n0	Conversation
     * 3	/m/02qldy	Narration, monologue
     * ...
     * @param audioClasses Probabilités des classes audio
     * @param activities Map des activités à remplir
     */
    private void mapAudioClassesToActivities(float[] audioClasses, Map<ActivityType, Double> activities) {
        // Mapping des classes audio vers des activités
        // Les valeurs exactes dépendent du modèle utilisé => à ajuster en fonction du modèle
        
        for (int i = 0; i < audioClasses.length; i++) {
            ThresholdSound[] activityList = correspondanceAudioIndexToAngelCategory.get(String.valueOf(i));
            if (activityList != null) {
                for (ThresholdSound activity : activityList) {
                    if (audioClasses[i] > activity.threshold) {
                        try {
                            ActivityType activityType = ActivityType.valueOf(activity.activity);
                            activities.put(activityType, (double) audioClasses[i]);
                        } catch (IllegalArgumentException e) {
                            logger.error("### Activity type '{}' does not exist !!!", activity.activity);
                        }
                    }
                }
            }
        }
    }
}
