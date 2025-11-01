package com.Softwer.compression.impl;

import com.Softwer.compression.core.ICompressor;
import java.util.Arrays;

/**
 * Version 3 : Compression avec zone de débordement (Overflow Area).
 * Implémentation complète avec optimisation et packing/unpacking.
 */
public class BitPackingOverflow implements ICompressor {

    private int[] compressedData;
    private int[] overflowArea;
    private int originalSize;

    // k' (taille totale d'un élément dans compressedData = 1 flag + p payload)
    private int bitsPerElement_k_prime;

    // p (taille du payload)
    private int payloadBits_p;

    // Valeur max stockable sans overflow (2^p - 1)
    private long maxValueNormal;

    // Masques pour le 'get'
    private long k_prime_mask;
    private long payloadMask;
    private long flagMask;

    /**
     * Calcule le nombre de bits minimum requis pour stocker 'maxValue'.
     */
    private int bitsNeeded(int maxValue) {
        if (maxValue == 0) {
            return 1;
        }
        return 32 - Integer.numberOfLeadingZeros(maxValue);
    }

    /**
     * Trouve le 'p' (bits de payload) optimal qui minimise la taille totale.
     * @param sortedData Données originales triées.
     * @return Le 'p' optimal (nombre de bits de payload).
     */
    private int findOptimalPayloadBits(int[] sortedData) {
        int n = sortedData.length;
        if (n == 0) return 0;

        int k_global_max = bitsNeeded(sortedData[n - 1]);

        // Coût initial (compression V1/V2)
        long minTotalCost = (long) n * k_global_max;
        int best_p = k_global_max; // Par défaut, V1 est le meilleur

        // Itérer sur toutes les largeurs de payload possibles
        for (int p = 1; p < k_global_max; p++) {

            long maxValueForP = (1L << p) - 1;

            // (a) Trouver N_overflow
            int n_overflow = 0;
            for (int i = n - 1; i >= 0; i--) {
                if (sortedData[i] > maxValueForP) {
                    n_overflow++;
                } else {
                    break; // Le tableau est trié
                }
            }

            // (b) Vérifier la Contrainte
            if (n_overflow > 0) {
                int bitsForPosition = bitsNeeded(n_overflow - 1);
                if (bitsForPosition > p) {
                    // Ce 'p' n'est pas valide
                    continue;
                }
            }

            // (c) Calculer le Coût Total pour 'p'
            int k_prime = 1 + p;
            long costMain = (long) n * k_prime;
            // Coût de stockage des exceptions (on suppose k_global_max bits par exception)
            long costOverflow = (long) n_overflow * k_global_max;
            long currentTotalCost = costMain + costOverflow;

            // (d) Mettre à jour le Meilleur
            if (currentTotalCost < minTotalCost) {
                minTotalCost = currentTotalCost;
                best_p = p;
            }
        }

        return best_p;
    }


    @Override
    public void compress(int[] originalArray) {
        this.originalSize = originalArray.length;
        if (this.originalSize == 0) {
            this.compressedData = new int[0];
            this.overflowArea = new int[0];
            return;
        }

        // 1. Trier une copie pour l'analyse
        int[] sortedData = Arrays.copyOf(originalArray, originalSize);
        Arrays.sort(sortedData);

        // 2. Trouver le 'p' optimal et configurer les variables
        this.payloadBits_p = findOptimalPayloadBits(sortedData);
        this.bitsPerElement_k_prime = 1 + this.payloadBits_p;
        this.maxValueNormal = (1L << this.payloadBits_p) - 1;

        // 3. Configurer les masques pour 'get'
        this.k_prime_mask = (1L << this.bitsPerElement_k_prime) - 1;
        this.payloadMask = (1L << this.payloadBits_p) - 1;
        this.flagMask = 1L << this.payloadBits_p;

        // 4. Allouer 'overflowArea'
        int n_overflow = 0;
        for (int value : originalArray) { // Parcourir l'original (non trié)
            if (value > this.maxValueNormal) {
                n_overflow++;
            }
        }
        this.overflowArea = new int[n_overflow];

        // 5. Allouer 'compressedData'
        long totalBitsNeeded = (long) this.originalSize * this.bitsPerElement_k_prime;
        int compressedSize = (int) Math.ceil((double) totalBitsNeeded / 32.0);
        this.compressedData = new int[compressedSize];

        // 6. Remplir les tableaux (Logique de Packing V1)
        int overflowIndexCounter = 0;

        for (int i = 0; i < this.originalSize; i++) {
            int value = originalArray[i];
            long valueToPack;

            if (value > this.maxValueNormal) {
                // Cas OVERFLOW
                this.overflowArea[overflowIndexCounter] = value;
                // Flag=1 | Payload=index
                valueToPack = this.flagMask | (long)overflowIndexCounter;
                overflowIndexCounter++;
            } else {
                // Cas NORMAL
                // Flag=0 | Payload=value
                valueToPack = (long) value;
            }

            // --- Logique de packing V1 ---
            long bitPosition = (long) i * this.bitsPerElement_k_prime;
            int arrayIndex = (int) (bitPosition / 32);
            int bitOffset = (int) (bitPosition % 32);

            // Appliquer le masque k_prime (assure que la valeur ne dépasse pas)
            long valueToPackMasked = valueToPack & this.k_prime_mask;

            // Créer la fenêtre de 64 bits décalée
            long shiftedValue = valueToPackMasked << bitOffset;

            // Appliquer la partie basse
            this.compressedData[arrayIndex] |= (int) shiftedValue;

            // Appliquer la partie haute si elle déborde
            if (arrayIndex + 1 < this.compressedData.length) {
                this.compressedData[arrayIndex + 1] |= (int) (shiftedValue >>> 32);
            }
            // --- Fin de la logique V1 ---
        }
    }

    @Override
    public int[] decompress() {
        int[] decompressedArray = new int[this.originalSize];

        // Appeler 'get' pour chaque élément
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

        // --- Logique d'unpacking V1 ---
        // 1. Calculer la position
        long bitPosition = (long) i * this.bitsPerElement_k_prime;
        int arrayIndex = (int) (bitPosition / 32);
        int bitOffset = (int) (bitPosition % 32);

        // 2. Lire le premier entier (partie basse)
        long lowBits = (long) this.compressedData[arrayIndex] & 0xFFFFFFFFL;

        // 3. Créer la fenêtre de 64 bits
        long combinedWindow = lowBits;

        // 4. Lire le deuxième entier (partie haute) si nécessaire
        if (arrayIndex + 1 < this.compressedData.length) {
            long highBits = (long) this.compressedData[arrayIndex + 1] & 0xFFFFFFFFL;
            combinedWindow |= (highBits << 32);
        }

        // 5. Décaler pour amener notre valeur au début
        long shiftedValue = combinedWindow >>> bitOffset;

        // 6. Appliquer le masque pour ne garder que nos 'k_prime' bits
        long packedValue = shiftedValue & this.k_prime_mask;
        // --- Fin de la logique V1 ---


        // --- Logique de Décodage V3 ---

        // Vérifier le bit de drapeau
        boolean isOverflow = (packedValue & this.flagMask) != 0;

        if (isOverflow) {
            // C'est un index : lire le payload et l'utiliser
            // comme index dans la zone de débordement
            int overflowIndex = (int) (packedValue & this.payloadMask);
            return this.overflowArea[overflowIndex];
        } else {
            // C'est une valeur directe : le payload est la valeur
            // (Le flagMask est déjà 0, donc un & avec payloadMask suffit,
            // mais packedValue est déjà correct)
            return (int) packedValue;
        }
    }
    // Dans BitPackingOverflow.java

    @Override
    public long getCompressedSizeInBits() {
        if (this.compressedData == null || this.overflowArea == null) {
            return 0;
        }
        long mainDataSize = (long) this.compressedData.length * 32;
        long overflowDataSize = (long) this.overflowArea.length * 32;
        return mainDataSize + overflowDataSize;
    }
}