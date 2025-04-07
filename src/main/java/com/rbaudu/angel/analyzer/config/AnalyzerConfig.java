package com.rbaudu.angel.analyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration centralisée pour l'analyseur Angel
 */
@Configuration
public class AnalyzerConfig {

    @Value("${angel.analyzer.audioAnalysisEnabled:false}")
    private boolean audioAnalysisEnabled;
    
    @Value("${angel.analyzer.audioClassificationModel:models/audio_classification/yamnet}")
    private String audioClassificationModel;
    
    @Value("${angel.analyzer.audioClassificationMapping:models/audio_classification/correspondance_yamnet_index_and_angel_category.csv}")
    private String audioClassificationMapping;
    
    @Value("${angel.analyzer.audioClassificationDefaultThreshold:0.5}")
    private double audioClassificationDefaultThreshold;
    
    @Value("${angel.analyzer.audioSampleRate:16000}")
    private float audioSampleRate;
    
    /**
     * Indique si l'analyse audio est activée
     * @return true si l'analyse audio est activée, false sinon
     */
    public boolean isAudioAnalysisEnabled() {
        return audioAnalysisEnabled;
    }
    
    /**
     * Chemin vers le modèle de classification audio
     * @return le chemin du modèle
     */
    public String getAudioClassificationModel() {
        return audioClassificationModel;
    }
    
    /**
     * Chemin vers le fichier de mapping des index de sons vers les catégories Angel
     * @return le chemin du fichier CSV
     */
    public String getAudioClassificationMapping() {
        return audioClassificationMapping;
    }
    
    /**
     * Seuil de confiance par défaut pour la prise en compte des sons
     * @return le seuil par défaut
     */
    public double getAudioClassificationDefaultThreshold() {
        return audioClassificationDefaultThreshold;
    }
    
    /**
     * Taux d'échantillonnage audio en Hz
     * @return le taux d'échantillonnage
     */
    public float getAudioSampleRate() {
        return audioSampleRate;
    }
}
