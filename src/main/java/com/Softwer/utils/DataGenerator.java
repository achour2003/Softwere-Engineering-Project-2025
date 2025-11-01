package com.Softwer.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Outil autonome pour générer des fichiers de données de test.
 * Accepte 3 arguments en ligne de commande :
 * 1. filename : Le nom du fichier à créer (ex: "data.txt")
 * 2. count : Le nombre d'entiers à générer (ex: 1000)
 * 3. upperBound : La valeur maximale exclusive (ex: 5000)
 */
public class DataGenerator {

    /**
     * Génère un fichier de données aléatoires.
     *
     * @param filename   Nom du fichier de sortie.
     * @param count      Nombre d'entiers à écrire.
     * @param upperBound Les entiers seront générés entre 0 (inclus) et cette valeur (exclusive).
     * @throws IOException Si une erreur d'écriture survient.
     */
    public static void generateDataFile(String filename, int count, int upperBound) throws IOException {
        Random rand = new Random();

        // Utilise un BufferedWriter pour des performances d'écriture efficaces
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < count; i++) {
                // Génère un entier aléatoire entre 0 (inclus) et upperBound (exclus)
                int value = rand.nextInt(upperBound);

                // Écrit la valeur suivie d'un saut de ligne
                writer.write(String.valueOf(value));
                writer.newLine();
            }
        }
    }

    /**
     * Point d'entrée pour l'outil de génération.
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java com.Softwer.utils.DataGenerator <filename> <count> <upperBound>");
            System.err.println("Exemple: java com.Softwer.utils.DataGenerator data.txt 10000 2048");
            return;
        }

        try {
            String filename = args[0];
            int count = Integer.parseInt(args[1]);
            int upperBound = Integer.parseInt(args[2]);

            if (count <= 0 || upperBound <= 0) {
                System.err.println("Erreur : 'count' et 'upperBound' doivent être positifs.");
                return;
            }

            System.out.println("Génération de " + count + " entiers (max=" + (upperBound - 1) + ") dans " + filename + "...");

            long startTime = System.nanoTime();
            generateDataFile(filename, count, upperBound);
            long endTime = System.nanoTime();

            System.out.println("Succès ! Fichier généré en " + (endTime - startTime) / 1_000_000 + " ms.");

        } catch (NumberFormatException e) {
            System.err.println("Erreur : 'count' et 'upperBound' doivent être des nombres entiers valides.");
        } catch (IOException e) {
            System.err.println("Erreur d'écriture lors de la création du fichier : " + e.getMessage());
        }
    }
}