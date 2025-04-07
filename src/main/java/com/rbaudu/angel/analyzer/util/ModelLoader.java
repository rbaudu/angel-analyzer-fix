package com.rbaudu.angel.analyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tensorflow.SavedModelBundle;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitaire pour charger les modèles TensorFlow
 */
@Component
public class ModelLoader {
    private static final Logger logger = LoggerFactory.getLogger(ModelLoader.class);
    
    /**
     * Charge un modèle TensorFlow SavedModel
     * 
     * @param modelPath Chemin vers le répertoire du modèle
     * @return Le bundle du modèle chargé
     * @throws Exception En cas d'erreur lors du chargement
     */
    public SavedModelBundle loadModel(String modelPath) throws Exception {
        File modelDir = new File(modelPath);
        
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            String message = "Le répertoire du modèle n'existe pas ou n'est pas un répertoire: " + modelPath;
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        
        try {
            logger.info("Chargement du modèle depuis {}", modelPath);
            SavedModelBundle model = SavedModelBundle.load(modelPath, "serve");
            logger.info("Modèle chargé avec succès");
            return model;
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du modèle depuis " + modelPath, e);
            throw e;
        }
    }
    
    /**
     * Vérifie si le modèle existe à l'emplacement spécifié
     * 
     * @param modelPath Chemin vers le répertoire du modèle
     * @return true si le modèle existe, false sinon
     */
    public boolean modelExists(String modelPath) {
        File modelDir = new File(modelPath);
        boolean exists = modelDir.exists() && modelDir.isDirectory();
        
        if (!exists) {
            logger.warn("Le modèle n'existe pas à l'emplacement: {}", modelPath);
        }
        
        return exists;
    }
}
