package com.Softwer.compression.factory;

import com.Softwer.compression.core.ICompressor;
import com.Softwer.compression.impl.BitPackingV1;
import com.Softwer.compression.impl.BitPackingV2;
import com.Softwer.compression.impl.BitPackingOverflow;

/**
 * Implémentation du Factory Method Pattern ("Creator").
 * Fournit une méthode statique pour créer les différents types
 * de compresseurs (Produits Concrets).
 *
 */
public class CompressorFactory {

    /**
     * Crée et retourne une instance du compresseur demandé.
     * @param type Le type de compresseur souhaité.
     * @return Une instance de ICompressor.
     */
    public static ICompressor createCompressor(CompressionType type) {
        switch (type) {
            case SPANNING_V1:
                return new BitPackingV1();
            case NON_SPANNING_V2:
                return new BitPackingV2();
            case OVERFLOW_V3:
                return new BitPackingOverflow();
            default:
                throw new IllegalArgumentException("Type de compression inconnu: " + type);
        }
    }
}