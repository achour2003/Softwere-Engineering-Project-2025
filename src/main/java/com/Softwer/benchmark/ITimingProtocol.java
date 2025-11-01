package com.Softwer.benchmark;

/**
 * Interface "Stratégie" (Strategy) pour le protocole de mesure de temps.
 * Permet de changer la manière dont on mesure le temps (ex: run unique,
 * moyenne sur N runs, avec ou sans warm-up JIT).
 *
 */
@FunctionalInterface // C'est maintenant correct !
public interface ITimingProtocol {

    /**
     * Mesure le temps d'exécution d'une opération.
     * @param operation L'opération à exécuter (fournie via une lambda).
     * @return Le temps d'exécution en nanosecondes.
     */
    long timeOperation(Runnable operation); // La SEULE méthode abstraite

    /**
     * Retourne une description du protocole utilisé.
     * Les classes qui implémentent cette interface peuvent (et devraient)
     * surcharger (override) cette méthode.
     */
    default String getProtocolDescription() {
        return "Protocole de mesure non spécifié";
    }
}