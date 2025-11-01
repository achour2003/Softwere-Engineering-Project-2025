Softwere-Engineering-Project-2025

Auteur : DJERADA Achour
Ce projet, réalisé dans le cadre du cours de Génie Logiciel, implémente et compare plusieurs algorithmes de compression d'entiers basés sur le Bit Packing. L'objectif est d'accélérer la transmission de tableaux d'entiers tout en conservant un accès direct aux éléments (get(i)).

Fonctionnalités Implémentées

Le programme compare trois algorithmes de compression distincts :

BitPackingV1 (Spanning) : Les entiers compressés peuvent être à cheval sur les frontières de 32 bits.

BitPackingV2 (Non-Spanning) : Chaque entier compressé est contenu dans un unique entier de 32 bits en sortie.

BitPackingV3 (Overflow) : Une implémentation hybride qui utilise un algorithme d'optimisation pour stocker les petites valeurs de manière compacte et les valeurs "exceptionnelles" dans une zone de débordement.

Architecture

Le projet est conçu autour de deux Design Patterns majeurs pour garantir la modularité et l'extensibilité :

Factory Method (CompressorFactory) : Gère la création des différents types de compresseurs (V1, V2, V3) via un paramètre unique (CompressionType), découplant ainsi le client des implémentations concrètes.

Strategy (ITimingProtocol) : Encapsule les différents protocoles de mesure de performance, permettant de changer la manière dont les benchmarks sont exécutés (ex: avec/sans warm-up) sans modifier le code de benchmark.

Comment Utiliser le Programme

Ce projet utilise Apache Maven pour gérer le cycle de vie de la compilation, des tests et de l'exécution. C'est la méthode recommandée pour garantir une construction identique sur tous les systèmes d'exploitation (Windows, macOS, Linux).

Prérequis

Java JDK (version 21+) : Assurez-vous que votre JDK est installé et que votre variable d'environnement JAVA_HOME est configurée.

Apache Maven : Assurez-vous que Maven est installé et que la commande mvn est accessible depuis votre terminal (la variable PATH doit inclure le dossier bin de Maven).

Étapes d'Exécution

Ouvrez un terminal à la racine du projet (là où se trouve le fichier pom.xml).

1. Compilation

Pour compiler l'ensemble du code source (y compris les tests) :

mvn compile


Maven téléchargera les dépendances (JUnit) et placera les fichiers .class dans le dossier target/classes et target/test-classes.

2. Exécution des Tests Unitaires

Pour lancer la suite de tests (située dans src/test/java) et s'assurer que les algorithmes sont logiquement corrects :

mvn test


Un rapport de succès ou d'échec s'affichera dans le terminal.

3. Étape A : Générer les Données de Test

Le programme principal a besoin d'un fichier data.txt. Pour le générer, utilisez la commande exec:java en ciblant la classe DataGenerator.

Syntaxe :

mvn exec:java -Dexec.mainClass="com.Softwer.utils.DataGenerator" -Dexec.args="<fichier> <nb_elements> <valeur_max>"


Exemple (10 000 entiers, max 99 999) :

mvn exec:java -Dexec.mainClass="com.Softwer.utils.DataGenerator" -Dexec.args="data.txt 10000 100000"


4. Étape B : Exécuter les Benchmarks

Une fois le fichier data.txt généré, exécutez le programme principal (Main) :

mvn exec:java -Dexec.mainClass="com.Softwer.Main"


Le programme lira data.txt, exécutera les tests de validation, puis lancera l'analyse comparative (Temps et Espace) des trois algorithmes.
