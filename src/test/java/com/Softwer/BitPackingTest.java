package com.Softwer;

import com.Softwer.compression.core.ICompressor;
import com.Softwer.compression.factory.CompressorFactory;
import com.Softwer.compression.factory.CompressionType;

// Imports JUnit 5
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les implémentations de ICompressor.
 * Ces tests valident la "Correction Logique" (pas la performance).
 */
public class BitPackingTest {

    /**
     * Test principal : vérifie le cycle complet "compress -> decompress"
     * et la précision de "get(i)" pour un tableau de données variées.
     *
     * Ce test sera exécuté 3 fois, une pour chaque type de CompressionType.
     *
     * @param type Le type de compresseur (V1, V2, V3) injecté par JUnit.
     */
    @DisplayName("Test de cycle complet (Compress, Decompress, Get)")
    @ParameterizedTest
    @EnumSource(CompressionType.class) // Exécute ce test pour chaque enum
    void testFullRoundTripAndGet(CompressionType type) {
        // 1. Arrange (Préparation)
        ICompressor compressor = CompressorFactory.createCompressor(type);
        int[] original = {1, 5, 0, 1024, 80, 511, 3, 2048, 99};

        // 2. Act (Action)
        compressor.compress(original);
        int[] decompressed = compressor.decompress();

        // 3. Assert (Vérification)

        // Vérifie decompress()
        assertArrayEquals(original, decompressed,
                "Le tableau décompressé doit être identique à l'original pour " + type);

        // Vérifie get(i)
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], compressor.get(i),
                    "get(" + i + ") doit retourner la bonne valeur pour " + type);
        }
    }

    /**
     * Test de cas limite : un tableau vide.
     */
    @DisplayName("Test de cas limite : Tableau Vide")
    @ParameterizedTest
    @EnumSource(CompressionType.class)
    void testEmptyArray(CompressionType type) {
        // 1. Arrange
        ICompressor compressor = CompressorFactory.createCompressor(type);
        int[] original = {};

        // 2. Act
        compressor.compress(original);
        int[] decompressed = compressor.decompress();

        // 3. Assert
        assertArrayEquals(original, decompressed,
                "Un tableau vide doit rester vide après compression/décompression pour " + type);
    }

    /**
     * Test de cas limite : un tableau avec un seul élément.
     */
    @DisplayName("Test de cas limite : Un Seul Élément")
    @ParameterizedTest
    @EnumSource(CompressionType.class)
    void testSingleElement(CompressionType type) {
        // 1. Arrange
        ICompressor compressor = CompressorFactory.createCompressor(type);
        int[] original = {42}; // 42 (k=6)

        // 2. Act
        compressor.compress(original);
        int[] decompressed = compressor.decompress();

        // 3. Assert
        assertArrayEquals(original, decompressed,
                "Un tableau à un seul élément doit être correct pour " + type);
        assertEquals(42, compressor.get(0),
                "get(0) sur un seul élément doit être correct pour " + type);
    }

    /**
     * Test de cas limite : un tableau ne contenant que des zéros.
     */
    @DisplayName("Test de cas limite : Tableau de Zéros")
    @ParameterizedTest
    @EnumSource(CompressionType.class)
    void testAllZeros(CompressionType type) {
        // 1. Arrange
        ICompressor compressor = CompressorFactory.createCompressor(type);
        int[] original = {0, 0, 0, 0, 0}; // k=1

        // 2. Act
        compressor.compress(original);
        int[] decompressed = compressor.decompress();

        // 3. Assert
        assertArrayEquals(original, decompressed,
                "Un tableau de zéros doit être correct pour " + type);
        assertEquals(0, compressor.get(3),
                "get(3) sur un tableau de zéros doit retourner 0 pour " + type);
    }

    /**
     * Test de cas limite : données nécessitant 17 bits.
     * Ceci teste le cas où V2 échoue en performance (0% d'économie)
     * mais doit rester *logiquement correct*.
     */
    @DisplayName("Test de cas limite : k=17 (échec de performance V2)")
    @ParameterizedTest
    @EnumSource(CompressionType.class)
    void testK17_V2PerformanceCase(CompressionType type) {
        // 1. Arrange
        ICompressor compressor = CompressorFactory.createCompressor(type);
        int[] original = {1, 99999, 50000, 131071}; // max = 131071 (k=17)

        // 2. Act
        compressor.compress(original);
        int[] decompressed = compressor.decompress();

        // 3. Assert
        assertArrayEquals(original, decompressed,
                "Le cycle doit être correct même pour k=17 pour " + type);
        assertEquals(99999, compressor.get(1),
                "get(1) doit retourner 99999 pour " + type);
    }
}