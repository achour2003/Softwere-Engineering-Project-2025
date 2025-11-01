package com.Softwer.benchmark;

/**
 * "Stratégie Concrète" : Un protocole de mesure de temps par défaut.
 * Implémente un warm-up pour le JIT (Just-In-Time Compiler)
 * et fait une moyenne sur plusieurs exécutions.
 *
 */
public class DefaultTimingProtocol implements ITimingProtocol {

    private final int WARMUP_RUNS = 10;
    private final int TIMING_RUNS = 100;

    @Override
    public long timeOperation(Runnable operation) {

        // TODO: 1. Phase de Warm-up (JIT)
        // for (int i = 0; i < WARMUP_RUNS; i++) {
        //     operation.run();
        // }

        // TODO: 2. Phase de Mesure
        // long totalTime = 0;
        // for (int i = 0; i < TIMING_RUNS; i++) {
        //     long startTime = System.nanoTime();
        //     operation.run();
        //     long endTime = System.nanoTime();
        //     totalTime += (endTime - startTime);
        // }

        // TODO: 3. Retourner la moyenne
        // return totalTime / TIMING_RUNS;

        // Implémentation simple pour l'instant :
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        return (endTime - startTime);
    }

    @Override
    public String getProtocolDescription() {
        return String.format(
                "Protocole par défaut : %d runs de warm-up, puis moyenne sur %d runs.",
                WARMUP_RUNS, TIMING_RUNS
        );
    }
}