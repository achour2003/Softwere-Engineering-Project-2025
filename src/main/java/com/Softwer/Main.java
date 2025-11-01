package com.Softwer;

import com.Softwer.benchmark.BenchmarkResult;
import com.Softwer.benchmark.BenchmarkRunner;
import com.Softwer.benchmark.DefaultTimingProtocol;
import com.Softwer.benchmark.ITimingProtocol;
import com.Softwer.compression.core.ICompressor;
import com.Softwer.compression.factory.CompressorFactory;
import com.Softwer.compression.factory.CompressionType;
import com.Softwer.utils.DataUtils; // <-- IMPORTATION
import java.io.IOException; // <-- IMPORTATION

public class Main {

    // Définir le fichier de données que le Main doit utiliser
    private static final String DATA_FILENAME = "data.txt";

    public static void main(String[] args) {
        System.out.println("Démarrage du projet de compression BitPacking...");

        // --- Lecture des données ---
        int[] data;
        try {
            data = DataUtils.readDataFromFile(DATA_FILENAME);
            System.out.println("Succès : " + data.length + " entiers lus depuis " + DATA_FILENAME);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erreur fatale lors de la lecture de '" + DATA_FILENAME + "'.");
            System.err.println("Assurez-vous d'avoir généré le fichier avec DataGenerator.");
            e.printStackTrace();
            return;
        }

        // Taille originale (calculée une seule fois)
        long originalSizeBits = (long) data.length * 32;

        // --- 1. Test V1 (Spanning) ---
        System.out.println("\nTest de la version V1 (Spanning)...");
        ICompressor compressorV1 = CompressorFactory.createCompressor(CompressionType.SPANNING_V1);
        runExample(compressorV1, data);

        // --- 2. Test V2 (Non-Spanning) ---
        System.out.println("\nTest de la version V2 (Non-Spanning)...");
        ICompressor compressorV2 = CompressorFactory.createCompressor(CompressionType.NON_SPANNING_V2);
        runExample(compressorV2, data);

        // --- 3. Test V3 (Overflow) ---
        System.out.println("\nTest de la version V3 (Overflow)...");
        ICompressor compressorV3 = CompressorFactory.createCompressor(CompressionType.OVERFLOW_V3);
        runExample(compressorV3, data);


        // --- 4. Lancement des Benchmarks ---
        System.out.println("\nLancement des Benchmarks...");

        ITimingProtocol protocol = new DefaultTimingProtocol();
        BenchmarkRunner runner = new BenchmarkRunner(protocol);

        // Exécuter et stocker les 3 résultats
        BenchmarkResult resultV1 = runner.run(compressorV1, data);
        BenchmarkResult resultV2 = runner.run(compressorV2, data);
        BenchmarkResult resultV3 = runner.run(compressorV3, data);

        // --- 5. Affichage des Scores Individuels ---
        // (Nous gardons les appels individuels car ils affichent
        //  le "Seuil de Rentabilité" que le tableau n'affiche pas)
        calculateBreakevenLatency(data, resultV1);
        calculateBreakevenLatency(data, resultV2);
        calculateBreakevenLatency(data, resultV3);

        // --- 6. NOUVEAU : Affichage du Tableau Comparatif ---
        printComparativeAnalysis(originalSizeBits, resultV1, resultV2, resultV3);
    }

    /**
     * Méthode utilitaire pour tester une implémentation de compresseur.
     */
    private static void runExample(ICompressor compressor, int[] data) {
        // 1. Compression
        compressor.compress(data);

        // 2. Test du 'get(i)'
        int indexToTest = data.length / 2; // Teste une valeur au milieu
        int value = compressor.get(indexToTest);
        System.out.printf("Test de get(%d) : Valeur attendue=%d, Obtenue=%d\n",
                indexToTest, data[indexToTest], value);

        // 3. Décompression
        int[] decompressedData = compressor.decompress();

        // 4. Vérification (simple)
        boolean match = java.util.Arrays.equals(data, decompressedData);
        System.out.println("Vérification (Compress -> Decompress): " + (match ? "SUCCÈS" : "ÉCHEC"));
    }


    /**
     * Calcule et affiche les scores de rentabilité en Espace et en Temps
     * pour un benchmark donné.
     *
     * @param originalData Les données de test originales.
     * @param result       Les résultats du benchmark (temps et taille).
     */
    private static void calculateBreakevenLatency(int[] originalData, BenchmarkResult result) {

        // --- 1. Score d'Espace (Complexité Spatiale) ---

        // Taille originale en bits (1 int = 32 bits)
        long originalSizeBits = (long) originalData.length * 32;
        long compressedSizeBits = result.getCompressedSizeInBits();
        long spaceSavedBits = originalSizeBits - compressedSizeBits;

        // Taux de compression (ex: 4.0 signifie 4x plus petit)
        double compressionRatio = 0.0;
        if (compressedSizeBits > 0) {
            compressionRatio = (double) originalSizeBits / compressedSizeBits;
        }

        // Pourcentage d'économie (ex: 80.0%)
        double spaceSavingPercent = 0.0;
        if (originalSizeBits > 0) {
            spaceSavingPercent = 100.0 * (double) spaceSavedBits / originalSizeBits;
        }

        // Nettoyer le nom du protocole pour l'affichage
        String protocolName = result.getProtocolDescription().split(":")[0];
        System.out.printf("\n--- Scores de Rentabilité (%s) ---%n", protocolName);
        System.out.println("Score d'Espace (Taux de Compression):");
        System.out.printf("  - Taille Originale  : %,d bits%n", originalSizeBits);
        System.out.printf("  - Taille Compressée : %,d bits%n", compressedSizeBits);
        System.out.printf("  - Taux de Compression : %.2f:1 (%.1f%% d'économie)%n",
                compressionRatio,
                spaceSavingPercent);


        // --- 2. Score de Temps (Seuil de Rentabilité) ---

        //[cite_start]// Le "temps de transmission pour une latence t" [cite: 14] est
        // l'inéquation :
        // Temps_avec_Compression < Temps_sans_Compression
        //
        // (T_comp + T_decomp) + T_transmission_comp < T_transmission_orig
        //
        // Soit B la bande passante (bits/sec) et T_overhead = T_comp + T_decomp
        // T_overhead_sec + (Size_comp / B) < (Size_orig / B)
        // T_overhead_sec < (Size_orig - Size_comp) / B
        // T_overhead_sec < spaceSavedBits / B
        //
        // B * T_overhead_sec < spaceSavedBits
        // B < spaceSavedBits / T_overhead_sec
        //
        // C'est le seuil de rentabilité (Breakeven Bandwidth).

        long timeOverheadNanos = result.getCompressTimeNanos() + result.getDecompressTimeNanos();
        double timeOverheadSeconds = timeOverheadNanos / 1_000_000_000.0;

        System.out.println("\nScore de Temps (Seuil de Rentabilité):");
        System.out.printf("  - Surcharge (Comp+Décomp): %.4f sec (%,d ns)%n",
                timeOverheadSeconds, timeOverheadNanos);

        // Cas où la compression n'économise pas d'espace
        if (spaceSavedBits <= 0) {
            System.out.println("  - Rentabilité (Vitesse): Jamais rentable.");
            System.out.println("  - Interprétation: Le gain d'espace est nul ou négatif.");
            System.out.println("-------------------------------------------------");
            return;
        }

        // Cas où le temps de calcul est quasi-nul (éviter la division par zéro)
        if (timeOverheadSeconds <= 0.0) {
            System.out.println("  - Rentabilité (Vitesse): Toujours rentable.");
            System.out.println("  - Interprétation: Le coût en temps est nul et l'espace est économisé.");
            System.out.println("-------------------------------------------------");
            return;
        }

        // B_breakeven = bits / secondes
        double B_breakeven_bps = (double) spaceSavedBits / timeOverheadSeconds;

        // Conversion en Mégabits par seconde (1 Mbit = 1,000,000 bits)
        double B_breakeven_mbps = B_breakeven_bps / 1_000_000.0;

        System.out.printf("  - Seuil de Rentabilité : %.2f Mbps%n", B_breakeven_mbps);
        System.out.println("  - Interprétation: La compression est rentable sur tout réseau");
        System.out.println("                   plus LENT que cette vitesse.");
        System.out.println("-------------------------------------------------");
    }
    /**
     * Affiche un tableau récapitulatif comparant les performances
     * en Espace et en Temps des trois algorithmes.
     *
     * @param originalSizeBits La taille originale des données.
     * @param resultV1         Résultats du benchmark V1.
     * @param resultV2         Résultats du benchmark V2.
     * @param resultV3         Résultats du benchmark V3.
     */
    private static void printComparativeAnalysis(long originalSizeBits,
                                                 BenchmarkResult resultV1,
                                                 BenchmarkResult resultV2,
                                                 BenchmarkResult resultV3) {

        System.out.println("\n\n--- Analyse Comparative Complète ---");
        System.out.println("-----------------------------------------------------------------------------");
        System.out.printf("| %-20s | %-17s | %-17s | %-17s |%n",
                "Métrique", "V1 (Spanning)", "V2 (Non-Spanning)", "V3 (Overflow)");
        System.out.println("|----------------------|-------------------|-------------------|-------------------|");

        // --- Comparaison d'Espace ---
        System.out.printf("| Taille Originale     | %,17d | %,17d | %,17d |%n",
                originalSizeBits, originalSizeBits, originalSizeBits);

        long sizeV1 = resultV1.getCompressedSizeInBits();
        long sizeV2 = resultV2.getCompressedSizeInBits();
        long sizeV3 = resultV3.getCompressedSizeInBits();

        System.out.printf("| Taille Compressée    | %,17d | %,17d | %,17d |%n", sizeV1, sizeV2, sizeV3);
        System.out.println("|----------------------|-------------------|-------------------|-------------------|");

        // Calcul des pourcentages d'économie
        double savingsV1 = 100.0 * (1.0 - (double) sizeV1 / originalSizeBits);
        double savingsV2 = 100.0 * (1.0 - (double) sizeV2 / originalSizeBits);
        double savingsV3 = 100.0 * (1.0 - (double) sizeV3 / originalSizeBits);

        System.out.printf("| ÉCONOMIE D'ESPACE    | %16.1f%% | %16.1f%% | %16.1f%% |%n",
                savingsV1, savingsV2, savingsV3);
        System.out.println("|----------------------|-------------------|-------------------|-------------------|");

        // --- Comparaison de Temps (en nanosecondes) ---
        System.out.printf("| Tps Compress (ns)    | %,17d | %,17d | %,17d |%n",
                resultV1.getCompressTimeNanos(),
                resultV2.getCompressTimeNanos(),
                resultV3.getCompressTimeNanos());

        System.out.printf("| Tps Décompress (ns)  | %,17d | %,17d | %,17d |%n",
                resultV1.getDecompressTimeNanos(),
                resultV2.getDecompressTimeNanos(),
                resultV3.getDecompressTimeNanos());

        System.out.printf("| Tps 'Get()' (moy, ns)| %,17d | %,17d | %,17d |%n",
                resultV1.getAverageGetTimeNanos(),
                resultV2.getAverageGetTimeNanos(),
                resultV3.getAverageGetTimeNanos());

        System.out.println("-----------------------------------------------------------------------------");
    }
}