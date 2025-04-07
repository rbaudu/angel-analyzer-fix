// Correction pour la classe AudioPatternDetector.java
// La ligne problématique était :
// resultTensor.copyTo(DataBuffers.of(resultBuffer));

// SOLUTION 1: Utilisation de NdArrays (recommandée)
// Cette approche crée un FloatNdArray à partir du tableau pour recevoir les données
int numClasses = 5; // Ajustez selon votre modèle
float[] audioClasses = new float[numClasses];
            
// Créer un FloatNdArray à partir du tableau pour recevoir les données
FloatNdArray targetArray = NdArrays.ofFloats(audioClasses);
            
// Copier les données du tensor vers notre tableau via le NdArray
resultTensor.copyTo(targetArray);

// SOLUTION 2: Utilisation directe des méthodes de TFloat32 (alternative)
// Si la solution 1 ne fonctionne pas pour une raison quelconque
int numClasses = 5; // Ajustez selon votre modèle
float[] audioClasses = new float[numClasses];

// Lire directement les valeurs du tensor
for (int i = 0; i < numClasses; i++) {
    audioClasses[i] = resultTensor.getFloat(0, i); // Ajustez les indices selon la forme de votre tensor
}

// SOLUTION 3: Utilisation de StdArrays (autre alternative)
// Cette approche utilise StdArrays pour convertir le tensor en tableau standard
int numClasses = 5; // Ajustez selon votre modèle

// Convertir le tensor directement en tableau
float[] audioClasses = resultTensor.data().asArray();
// Note: Si le tensor a une forme différente, vous pourriez avoir besoin 
// d'extraire les données différemment ou de remodeler le tableau résultant

// IMPORTS NÉCESSAIRES
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.StdArrays; // Pour solution 3
