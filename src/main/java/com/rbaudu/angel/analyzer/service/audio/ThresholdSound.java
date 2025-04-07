package com.rbaudu.angel.analyzer.service.audio;

/**
 * Classe utilitaire représentant une activité avec son seuil de détection.
 * Utilisée pour le mapping entre les sons Yamnet et les catégories d'activités Angel.
 */
public class ThresholdSound {
    String activity;
    double threshold;
    
    /**
     * Constructeur.
     * 
     * @param activity Code de l'activité (correspondant à une enum ActivityType)
     * @param threshold Seuil de confiance pour la prise en compte de cette activité
     */
    ThresholdSound(String activity, double threshold) {
        this.activity = activity;
        this.threshold = threshold;
    }
    
    @Override
    public String toString() {
        return activity + "*" + threshold;
    }
}
