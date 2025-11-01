package com.Softwer.benchmark;

// Imports nécessaires
import com.Softwer.compression.core.ICompressor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Classe "Contexte" (Context) du Strategy Pattern.
 * Elle *utilise* une stratégie de timing (ITimingProtocol) pour
 * exécuter les benchmarks sur un compresseur (ICompressor).
 * (Version mise à jour pour inclure la mesure de la taille)
 */
public class BenchmarkRunner {

    private ITimingProtocol protocol;

    // Injection de la stratégie par le constructeur
    public BenchmarkRunner(ITimingProtocol protocol) {
        this.protocol = protocol;
    }

    // Permet de changer la stratégie à la volée
    public void setProtocol(ITimingProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Méthode 'run' mise à jour pour récupérer la taille compressée.
     */
    public BenchmarkResult run(ICompressor compressor, int[] data) {

        // 1. Mesurer la compression
        long compressTime = protocol.timeOperation(() -> {
            compressor.compress(data);
        });

        // --- LIGNE AJOUTÉE ---
        // Récupérer la taille juste après la compression
        long sizeInBits = compressor.getCompressedSizeInBits();
        // ---------------------

        // 2. Mesurer la décompression
        long decompressTime = protocol.timeOperation(() -> {
            compressor.decompress();
        });

        // 3. Mesurer le 'get' (en moyenne)
        AtomicLong totalGetTime = new AtomicLong(0);

        // Note: Le protocole de 'get' est simpliste et peut être amélioré
        // en utilisant 'timeOperation' pour la boucle complète, mais
        // cela complique la capture de 'totalGetTime'.
        // Nous mesurons le temps total de N 'get' à l'intérieur d'un
        // 'timeOperation' qui ne fait... rien (ou presque).

        // Exécution de la boucle 'get' pour obtenir le temps total
        // (Cette partie est complexe à mesurer correctement à cause du JIT)
        // Nous allons utiliser une approche plus simple :
        // Mesurer le temps total de N appels 'get'

        long startGetTotal = System.nanoTime();
        for (int i = 0; i < data.length; i++) {
            compressor.get(i);
        }
        long endGetTotal = System.nanoTime();
        totalGetTime.set(endGetTotal - startGetTotal);

        long averageGetTime = (data.length == 0) ? 0 : (totalGetTime.get() / data.length);


        // --- RETOUR MIS À JOUR ---
        // Passe 'sizeInBits' au constructeur de BenchmarkResult
        return new BenchmarkResult(
                compressTime,
                decompressTime,
                averageGetTime,
                sizeInBits, // Argument ajouté
                protocol.getProtocolDescription()
        );
    }
}