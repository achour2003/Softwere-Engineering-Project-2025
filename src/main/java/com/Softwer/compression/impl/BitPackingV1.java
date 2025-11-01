package com.Softwer.compression.impl;

import com.Softwer.compression.core.ICompressor;
import java.util.Arrays;

/**
 * Version 1 : "spanning"
 * Les entiers compressés peuvent être à cheval sur deux entiers de sortie.
 *
 */
public class BitPackingV1 implements ICompressor {

    private int[] compressedData;
    private int bitsPerElement; // 'k'
    private int originalSize;
    private long mask; // Masque pour 'get', doit être un 'long'

    /**
     * Calcule le nombre de bits minimum requis pour stocker 'maxValue'.
     */
    private int bitsNeeded(int maxValue) {
        if (maxValue == 0) {
            return 1;
        }
        return 32 - Integer.numberOfLeadingZeros(maxValue);
    }

    @Override
    public void compress(int[] originalArray) {
        this.originalSize = originalArray.length;
        if (this.originalSize == 0) {
            this.compressedData = new int[0];
            return;
        }

        // 1. Trouver le nombre de bits 'k' max nécessaire.
        int maxValue = Arrays.stream(originalArray).max().orElse(0);
        this.bitsPerElement = bitsNeeded(maxValue); // 'k'

        // 2. Allouer 'compressedData'
        // Taille totale en bits = n * k
        long totalBitsNeeded = (long) this.originalSize * this.bitsPerElement;
        // Taille en entiers de 32 bits (arrondi au supérieur)
        int compressedSize = (int) Math.ceil((double) totalBitsNeeded / 32.0);
        this.compressedData = new int[compressedSize];

        // 3. Créer le masque pour 'get' (sur 64 bits pour 'long')
        // Si k=12, (1L << 12) - 1 = 4096 - 1 = 4095
        this.mask = (1L << this.bitsPerElement) - 1;

        // 4. Écrire les bits en permettant le chevauchement (spanning).
        for (int i = 0; i < this.originalSize; i++) {
            long bitPosition = (long) i * this.bitsPerElement;
            int arrayIndex = (int) (bitPosition / 32);
            int bitOffset = (int) (bitPosition % 32);

            // 'value' est l'entier à compresser.
            // On le caste en 'long' et on s'assure qu'il est non-signé
            // (le & 0xFFFFFFFFL gère les nombres négatifs si on les supportait)
            long value = (long) originalArray[i] & 0xFFFFFFFFL;

            // On décale la valeur à sa position dans une "fenêtre" 64 bits
            long shiftedValue = value << bitOffset;

            // Appliquer la partie basse du long (32 bits) à compressedData[arrayIndex]
            this.compressedData[arrayIndex] |= (int) shiftedValue;

            // Si la valeur est à cheval (dépasse 32 bits après décalage)
            // et qu'on n'est pas au dernier index
            if (arrayIndex + 1 < this.compressedData.length) {
                // Appliquer la partie haute du long (les 32 bits suivants)
                // à compressedData[arrayIndex + 1]
                this.compressedData[arrayIndex + 1] |= (int) (shiftedValue >>> 32);
            }
        }
    }

    @Override
    public int[] decompress() {
        int[] decompressedArray = new int[this.originalSize];

        // La méthode 'get(i)' contient toute la logique d'extraction.
        // Il suffit de l'appeler en boucle.
        for (int i = 0; i < this.originalSize; i++) {
            decompressedArray[i] = this.get(i);
        }

        return decompressedArray;
    }

    @Override
    public int get(int i) {
        if (i < 0 || i >= this.originalSize) {
            throw new IndexOutOfBoundsException("Index " + i + " hors limites pour la taille " + this.originalSize);
        }

        // 1. Calculer la position en bits (sur un long)
        long bitPosition = (long) i * this.bitsPerElement;

        // 2. Déterminer l'index (ou les deux index) dans 'compressedData'
        int arrayIndex = (int) (bitPosition / 32);
        int bitOffset = (int) (bitPosition % 32);

        // 3. Extraire la valeur

        // a. Récupérer l'entier de 32 bits contenant le début de notre valeur
        // On le caste en 'long' et on gère le non-signé
        long lowBits = (long) this.compressedData[arrayIndex] & 0xFFFFFFFFL;

        // b. Créer notre "fenêtre" de 64 bits
        long combinedWindow = lowBits;

        // c. Si on a besoin de l'entier suivant (si on n'est pas au bord
        //    et si les bits débordent)
        if (arrayIndex + 1 < this.compressedData.length) {
            long highBits = (long) this.compressedData[arrayIndex + 1] & 0xFFFFFFFFL;
            // On décale les bits hauts de 32 et on les combine
            combinedWindow |= (highBits << 32);
        }

        // d. Décaler la fenêtre combinée vers la droite pour amener
        //    notre valeur au début
        long shiftedValue = combinedWindow >>> bitOffset;

        // e. Appliquer le masque pour ne garder que nos 'k' bits
        return (int) (shiftedValue & this.mask);
    }
    @Override
    public long getCompressedSizeInBits() {
        if (this.compressedData == null) {
            return 0;
        }
        // 1 entier Java = 32 bits
        return (long) this.compressedData.length * 32;
    }
}