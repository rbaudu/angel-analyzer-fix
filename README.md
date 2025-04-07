# Correction pour AudioPatternDetector - TensorFlow 0.5.0

Ce dépôt contient les corrections nécessaires pour adapter le code de `AudioPatternDetector.java` du projet angel-server-capture à TensorFlow Java 0.5.0.

## Problème

Suite à la mise à jour de TensorFlow vers la version 0.5.0, la classe `AudioPatternDetector` génère une erreur au niveau de la ligne :

```java
resultTensor.copyTo(DataBuffers.of(resultBuffer));
```

Erreur : `The method copyTo(NdArray) in the type FloatNdArray is not applicable for the arguments (FloatDataBuffer)`

## Solution

Dans TensorFlow 0.5.0, la méthode `copyTo()` attend un paramètre de type `NdArray` et non plus un `DataBuffer`. Voici la solution recommandée :

```java
// Remplacer ces lignes :
int numClasses = 5; // Supposons 5 types de sons identifiables
float[] audioClasses = new float[numClasses];
FloatBuffer resultBuffer = FloatBuffer.allocate(numClasses);
resultTensor.copyTo(DataBuffers.of(resultBuffer));
resultBuffer.position(0);  // Rewind the buffer
resultBuffer.get(audioClasses);

// Par ceci :
int numClasses = 5; // Supposons 5 types de sons identifiables
float[] audioClasses = new float[numClasses];
            
// Créer un FloatNdArray à partir du tableau pour recevoir les données
FloatNdArray targetArray = NdArrays.ofFloats(audioClasses);
            
// Copier les données du tensor vers notre tableau via le NdArray
resultTensor.copyTo(targetArray);
```

## Importations nécessaires

Assurez-vous d'ajouter ces importations :

```java
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.NdArrays;
```

## Alternatives

Si la solution principale ne fonctionne pas pour votre cas d'usage, deux alternatives sont disponibles dans le fichier `AudioPatternDetector-Fix.java`.

## Comment intégrer la correction

1. Ouvrez votre fichier `AudioPatternDetector.java` dans le projet angel-server-capture
2. Recherchez le bloc de code concernant l'extraction des résultats du tensor
3. Remplacez-le par la solution recommandée ci-dessus
4. Ajoutez les imports nécessaires
5. Compilez et testez votre application

## Remarques importantes

- Assurez-vous que la variable `numClasses` correspond bien au nombre de classes attendues en sortie de votre modèle
- Si votre tensor a une forme différente, vous devrez peut-être ajuster la création du `NdArray` cible
