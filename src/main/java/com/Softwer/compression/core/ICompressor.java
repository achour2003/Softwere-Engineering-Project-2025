package com.Softwer.compression.core;

/**
 * Interface "Produit" (Product) de notre Factory Pattern.
 * Définit le contrat pour toutes les méthodes de compression BitPacking.
 * Les implémentations doivent être "stateful" (elles conservent l'état
 * des données compressées).
 */
public interface ICompressor {

    /**
     * Compresse un tableau d'entiers et le stocke en interne.
     * [cite: 11]
     * @param originalArray Le tableau d'entiers à compresser.
     */
    void compress(int[] originalArray);

    /**
     * Décompresse les données stockées en interne et les retourne.
     * [cite: 11]
     * @return Un nouveau tableau contenant les entiers décompressés.
     */
    int[] decompress();

    /**
     * Retourne la valeur du i-ème entier dans le tableau *original*,
     * en y accédant depuis la structure compressée.
     * [cite: 12]
     * @param i L'index de l'élément (basé sur le tableau original).
     * @return La valeur de l'entier original.
     */
    int get(int i);
    /**
     * Retourne la taille totale en BITS des données compressées
     * qui seraient nécessaires à la transmission.
     *
     * @return La taille totale en bits.
     */
    long getCompressedSizeInBits();
}