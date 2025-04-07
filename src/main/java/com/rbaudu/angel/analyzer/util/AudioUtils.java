package com.rbaudu.angel.analyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.types.TFloat32;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utilitaires pour le traitement audio
 */
@Component
public class AudioUtils {
    private static final Logger logger = LoggerFactory.getLogger(AudioUtils.class);

    /**
     * Convertit un flux audio au format cible
     * 
     * @param audioStream Flux audio source
     * @param targetFormat Format audio cible
     * @return Flux audio converti
     * @throws Exception En cas d'erreur lors de la conversion
     */
    public AudioInputStream convertAudioFormat(AudioInputStream audioStream, AudioFormat targetFormat) throws Exception {
        if (audioStream.getFormat().matches(targetFormat)) {
            return audioStream;
        }
        
        try {
            return AudioSystem.getAudioInputStream(targetFormat, audioStream);
        } catch (Exception e) {
            logger.error("Erreur lors de la conversion du format audio", e);
            throw e;
        }
    }
    
    /**
     * Convertit les données PCM en tableau de flottants
     * 
     * @param pcmData Données audio au format PCM
     * @return Tableau de flottants normalisés entre -1.0 et 1.0
     */
    public float[] pcmToFloat(byte[] pcmData) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN);
        float[] floatData = new float[pcmData.length / 2];
        
        for (int i = 0; i < floatData.length; i++) {
            // Conversion de 16 bits signés à float
            floatData[i] = byteBuffer.getShort() / 32768.0f;
        }
        
        return floatData;
    }
    
    /**
     * Extrait les coefficients cepstraux sur l'échelle de Mel (MFCC) à partir des données audio
     * 
     * @param audioData Données audio brutes
     * @param sampleRate Taux d'échantillonnage
     * @param numCoefficients Nombre de coefficients à extraire
     * @return Tableau des coefficients MFCC
     */
    public float[] extractMFCC(byte[] audioData, float sampleRate, int numCoefficients) {
        // Implémentation simplifiée - dans un cas réel, il faudrait utiliser une bibliothèque spécialisée
        // comme librosa ou jLibrosa pour Java
        
        // Conversion en échantillons flottants
        float[] samples = pcmToFloat(audioData);
        
        // Simulation d'extraction MFCC (à remplacer par une vraie implémentation)
        float[] mfcc = new float[numCoefficients * 100]; // Supposons 100 fenêtres temporelles
        
        // Ici, vous devriez implémenter l'extraction réelle des MFCC
        // Cette implémentation factice remplit simplement le tableau avec des valeurs
        for (int i = 0; i < mfcc.length; i++) {
            mfcc[i] = (float) Math.random() * 2 - 1; // Valeurs entre -1 et 1
        }
        
        logger.debug("MFCC extraits: {} coefficients", mfcc.length);
        return mfcc;
    }
    
    /**
     * Convertit un tableau de caractéristiques audio en tensor TensorFlow
     * 
     * @param features Caractéristiques audio (par exemple, MFCC)
     * @return Tensor TensorFlow
     */
    public TFloat32 audioToTensor(float[] features) {
        // Création d'un tensor de forme adaptée au modèle
        // La forme exacte dépend du modèle utilisé
        long[] shape = {1, features.length}; // Batch de 1, longueur des caractéristiques
        
        FloatNdArray ndArray = NdArrays.ofFloats(shape);
        
        // Remplissage du tensor avec les caractéristiques
        for (int i = 0; i < features.length; i++) {
            ndArray.setFloat(features[i], 0, i);
        }
        
        return TFloat32.tensorOf(ndArray);
    }
    
    /**
     * Calcule l'énergie du signal (RMS)
     * 
     * @param samples Échantillons audio
     * @return Valeur RMS
     */
    public double calculateRMS(float[] samples) {
        double sum = 0.0;
        for (float sample : samples) {
            sum += sample * sample;
        }
        return Math.sqrt(sum / samples.length);
    }
}
