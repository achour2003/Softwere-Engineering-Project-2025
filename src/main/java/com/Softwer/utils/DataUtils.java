package com.Softwer.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Classe utilitaire pour les opérations sur les données,
 * comme la lecture depuis un fichier.
 */
public class DataUtils {

    /**
     * Lit un fichier texte où chaque ligne est un entier
     * et le convertit en un tableau de int[].
     *
     * @param filename Le chemin vers le fichier.
     * @return Un tableau d'entiers.
     * @throws IOException Si le fichier n'est pas trouvé ou ne peut pas être lu.
     * @throws NumberFormatException Si une ligne n'est pas un entier valide.
     */
    public static int[] readDataFromFile(String filename) throws IOException, NumberFormatException {

        // Lit toutes les lignes du fichier
        List<String> lines = Files.readAllLines(Paths.get(filename));

        // Utilise l'API Stream pour convertir la List<String> en int[]
        // .mapToInt(Integer::parseInt) applique la fonction Integer.parseInt à chaque ligne
        // .toArray() convertit le flux en tableau
        return lines.stream()
                .mapToInt(Integer::parseInt)
                .toArray();
    }
}