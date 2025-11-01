package com.Softwer.benchmark;

/**
 * Un simple POJO (Plain Old Java Object) pour stocker les
 * résultats d'un benchmark.
 * (Version mise à jour pour inclure la taille)
 */
public class BenchmarkResult {
    private final long compressTimeNanos;
    private final long decompressTimeNanos;
    private final long averageGetTimeNanos;
    private final long compressedSizeInBits; // CHAMP AJOUTÉ
    private final String protocolDescription;

    /**
     * Constructeur mis à jour pour accepter la taille compressée.
     */
    public BenchmarkResult(long compressTime, long decompressTime, long avgGetTime,
                           long compressedSize, String protocol) { // PARAMÈTRE AJOUTÉ
        this.compressTimeNanos = compressTime;
        this.decompressTimeNanos = decompressTime;
        this.averageGetTimeNanos = avgGetTime;
        this.compressedSizeInBits = compressedSize; // AFFECTATION AJOUTÉE
        this.protocolDescription = protocol;
    }

    /**
     * @return La description du protocole de timing utilisé pour ce résultat.
     */
    public String getProtocolDescription() {
        return this.protocolDescription;
    }

    // --- GETTER AJOUTÉ ---
    /**
     * @return La taille totale des données compressées en bits.
     */
    public long getCompressedSizeInBits() {
        return this.compressedSizeInBits;
    }
    // ---------------------

    // Getters existants
    public long getCompressTimeNanos() { return compressTimeNanos; }
    public long getDecompressTimeNanos() { return decompressTimeNanos; }
    public long getAverageGetTimeNanos() { return averageGetTimeNanos; }

    @Override
    public String toString() {
        // 'toString' mis à jour pour afficher la taille
        return String.format(
                "\n--- Résultats du Benchmark ---" +
                        "\nProtocole: %s" +
                        "\nCompression   : %,d ns" +
                        "\nDécompression : %,d ns" +
                        "\nAccès 'get()' (moy): %,d ns" +
                        "\nTaille Compressée : %,d bits" + // LIGNE AJOUTÉE
                        "\n----------------------------",
                protocolDescription, compressTimeNanos, decompressTimeNanos, averageGetTimeNanos,
                compressedSizeInBits // VARIABLE AJOUTÉE
        );
    }
}