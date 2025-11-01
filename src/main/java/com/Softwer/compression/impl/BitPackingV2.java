package com.Softwer.compression.impl;

import com.Softwer.compression.core.ICompressor;
import java.util.Arrays;

/**
 * Version 2 : "non-spanning"
 * Les entiers compressés sont entièrement contenus dans un seul entier de sortie.
 *
 * Utilise des entiers 32 bits comme conteneurs.
 */
public class BitPackingV2 implements ICompressor {

    private int[] compressedData;
    private int bitsPerElement; // 'k'
    private int originalSize;
    private int elementsPerInteger; // Nombre d'éléments de 'k' bits qui rentrent dans 32 bits
    private int mask; // Masque pour extraire un élément (ex: 000...01111 si k=4)

    /**
     * Calcule le nombre de bits minimum requis pour stocker 'maxValue'.
     * Par exemple, 5 (101) a besoin de 3 bits. 7 (111) a besoin de 3 bits. 8 (1000) a besoin de 4 bits.
     */
    private int bitsNeeded(int maxValue) {
        if (maxValue == 0) {
            return 1; // Il faut au moins 1 bit pour stocker '0'
        }
        // Calcule le logarithme en base 2 et arrondit à l'entier supérieur.
        // Equivalent à trouver la position du bit de poids le plus fort + 1.
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
        // (Note: .max() retourne un OptionalInt, .orElse(0) gère le cas du tableau vide)
        int maxValue = Arrays.stream(originalArray).max().orElse(0);
        this.bitsPerElement = bitsNeeded(maxValue); // 'k'

        // 2. Calculer 'elementsPerInteger' = 32 / k.
        // (Ex: si k=10, on peut en mettre 3 par entier de 32 bits, 32/10 = 3)
        this.elementsPerInteger = 32 / this.bitsPerElement;

        // 3. Allouer 'compressedData'
        // (Ex: 100 éléments, 3 par entier -> 100/3 = 33.3 -> 34 entiers)
        int compressedSize = (int) Math.ceil((double) this.originalSize / this.elementsPerInteger);
        this.compressedData = new int[compressedSize];

        // 4. Créer le masque pour 'get'
        // Si k=5, on veut (1 << 5) - 1 = 32 - 1 = 31 (binaire 0...011111)
        this.mask = (1 << this.bitsPerElement) - 1;

        // 5. Écrire les entiers dans 'compressedData' sans chevauchement.
        for (int i = 0; i < this.originalSize; i++) {
            int arrayIndex = i / this.elementsPerInteger;
            int localIndex = i % this.elementsPerInteger;
            int bitOffset = localIndex * this.bitsPerElement;

            // 'value' est l'entier à compresser
            int value = originalArray[i];

            // On décale la valeur à sa position dans l'entier de 32 bits
            // (Ex: si bitOffset=8, on décale la valeur de 8 bits vers la gauche)
            int shiftedValue = value << bitOffset;

            // On utilise un OU bit-à-bit pour insérer la valeur dans le tableau compressé
            // sans écraser les autres valeurs déjà présentes.
            this.compressedData[arrayIndex] = this.compressedData[arrayIndex] | shiftedValue;
        }
    }

    @Override
    public int[] decompress() {
        int[] decompressedArray = new int[this.originalSize];

        // La méthode 'get(i)' contient déjà toute la logique d'extraction.
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

        // 1. Trouver l'index dans 'compressedData'
        int arrayIndex = i / this.elementsPerInteger;

        // 2. Trouver la position à l'intérieur de l'entier
        int localIndex = i % this.elementsPerInteger;

        // 3. Calculer l'offset en bits
        int bitOffset = localIndex * this.bitsPerElement;

        // 4. Extraire la valeur

        // a. Récupérer l'entier de 32 bits contenant notre valeur
        int packedValue = this.compressedData[arrayIndex];

        // b. Décaler les bits vers la droite pour amener notre valeur au début
        // (Ex: si offset=8, on décale de 8 bits. Le '>>>' est un décalage
        // non signé, qui remplit avec des 0 à gauche, essentiel si
        // le bit de poids fort est 1).
        int shiftedValue = packedValue >>> bitOffset;

        // c. Appliquer le masque pour ne garder que nos 'k' bits et
        // mettre tous les autres bits (à gauche) à 0.
        return shiftedValue & this.mask;
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