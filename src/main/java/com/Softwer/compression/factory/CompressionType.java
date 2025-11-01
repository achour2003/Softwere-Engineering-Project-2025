package com.Softwer.compression.factory;

/**
 * Énumération pour fournir le "paramètre unique" à la Factory,
 * afin de sélectionner le type de compresseur à créer.
 *
 */
public enum CompressionType {
    /**
     * Version 1 : Les entiers compressés peuvent être à cheval
     * sur deux entiers de sortie consécutifs.
     * [cite: 9]
     */
    SPANNING_V1,

    /**
     * Version 2 : Les entiers compressés ne doivent *pas* être à cheval
     * sur deux entiers de sortie.
     * [cite: 9]
     */
    NON_SPANNING_V2,

    /**
     * Version 3 : Compression utilisant des bits de
     * débordement (overflow areas).
     * [cite: 15]
     */
    OVERFLOW_V3
}