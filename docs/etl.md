# Documentation Détaillée de l'Outil ETL COVID-19

## 1. Contexte et Objectifs de l'ETL

Cet outil ETL (Extract, Transform, Load) a été développé pour répondre au besoin de consolidation et de structuration des données hétérogènes relatives à la pandémie de COVID-19. Face à la multitude de sources de données, souvent avec des formats et des niveaux de granularité différents, il est devenu essentiel de disposer d'un processus automatisé pour :

- **Centraliser** les informations clés issues de différentes sources reconnues (Worldometer, JHU CSSE via Kaggle, etc.).

- **Nettoyer et Harmoniser** les données pour assurer leur cohérence (ex: standardisation des noms de pays/régions, format des nombres).

- **Structurer** ces données dans un modèle relationnel optimisé pour les requêtes et les analyses futures.

- **Alimenter** une base de données unique qui peut servir de source fiable pour d'autres applications, des tableaux de bord de suivi, ou des analyses statistiques et épidémiologiques.

L'objectif final est de transformer des fichiers CSV bruts en une base de données relationnelle propre, cohérente et exploitable concernant les cas de COVID-19, les localisations géographiques et les métadonnées associées.

## 2. Périmètre Détaillé de l'ETL

### 2.1 Sources de Données Traitées

L'ETL est configuré pour traiter les fichiers CSV suivants, attendus dans un répertoire spécifique accessible par l'application :

1.  **`worldometer_data.csv`**: Fournit des statistiques globales agrégées par pays. Contient des informations telles que le nom du pays, le continent, la population, le total des cas confirmés, des décès, des guérisons, le nombre de cas actifs et la région OMS associée. Ce fichier est crucial pour établir la structure géographique de base (Pays, Continents).

2.  **`covid_19_clean_complete.csv`**: Contient des données de cas journaliers, potentiellement plus granulaires (par province/état en plus du pays). Inclut typiquement la date, la province/état, le pays/région, latitude, longitude, cas confirmés, décès, guérisons. Ces données permettent un suivi temporel et géographique plus fin.

3.  **`full_grouped.csv`**: Semble être une agrégation journalière par pays, incluant la date, le pays, les cas confirmés, les décès, les guérisons, les nouveaux cas, les nouveaux décès, les nouvelles guérisons et la région OMS. Il complète potentiellement les données des autres sources avec des indicateurs journaliers agrégés.

4.  **`usa_county_wise.csv`**: Spécifique aux États-Unis, ce fichier descend au niveau du comté. Il contient des informations détaillées par comté, incluant le nom du comté, l'état, le FIPS code, latitude, longitude, population, et les chiffres de cas/décès. Essentiel pour une analyse fine sur le territoire américain.

### 2.2 Déroulement Simplifié du Processus ETL

Le traitement complet des données est géré par le `DataImportCovid19Runner` lorsqu'on lance l'application. Voici les étapes clés, décomposées pour plus de clarté :

1.  **Initialisation (Fait par `DataImportCovid19Runner`)**

- **Nettoyage Prévu**: Avant de commencer, le Runner demande la suppression de toutes les données COVID précédentes dans la base de données (Tables: `DiseaseCase`, `Disease`, `Location`, `Region`, `Country`). Cela assure que chaque exécution part d'une base propre.

- **Préparation Maladie**: Il s'assure que la maladie "COVID-19" existe dans la base et la garde en mémoire (cache) pour une utilisation rapide.

2.  **Traitement Séquentiel des Fichiers (Piloté par `DataImportCovid19Runner`, Exécuté par les `Services`)**

- Le Runner lance le traitement pour chaque fichier CSV, l'un après l'autre (`worldometer`, `covid_19_clean_complete`, etc.). Chaque fichier a son propre `Service` dédié (ex: `WorldometerService`).

- Pour un fichier donné (prenons `worldometer_data.csv` comme exemple) :

a. **Étape 1 : Lecture du Fichier (Fait par `AbstractCsvImportService` et `CsvReaderHelper`)**

- Le service trouve le fichier CSV (`worldometer_data.csv`).

- Le `CsvReaderHelper` lit le fichier ligne par ligne.

- Chaque ligne est découpée en colonnes.

b. **Étape 2 : Transformation de Chaque Ligne (Fait par `WorldometerService`, `WorldometerMapper`, `CleanerHelper`, `CacheManager`)**

- **Validation/Nettoyage Simple (`WorldometerService`)**: Vérifie si la ligne a assez de colonnes. Transforme les textes en nombres, enlève les espaces inutiles. Si une ligne a un problème (ex: mauvais format de nombre), elle est ignorée avec un message d'alerte.

- **Nettoyage Avancé (`CleanerHelper`)**: Standardise les noms importants (ex: "US", "USA", "United States" deviennent tous "United States"). Ignore certaines lignes si le pays est dans une liste prédéfinie (`skipList`).

- **Création Objet Temporaire (`WorldometerService`)**: Les données nettoyées de la ligne sont mises dans un objet simple appelé DTO (`WorldometerDto`).

- **Transformation en Données Finales (`WorldometerMapper` avec `CacheManager`)**: C'est le cœur du travail !

  - Le `Mapper` prend le DTO (`WorldometerDto`).

  - Il demande au `CacheManager` : "As-tu déjà un Pays, une Région, une Localisation correspondant à ces noms ?".

  - Le `CacheManager` regarde dans sa mémoire (des `Map` Java).

    - Si **Oui** : il renvoie l'objet existant (ex: l'objet `Country` pour "France").

    - Si **Non** : il crée un nouvel objet (ex: `new Country()`) mais **ne le sauvegarde pas encore en base**. Il le met dans sa mémoire et le renvoie.

  - Le `Mapper` met à jour les informations de l'objet (ex: la population du `Country`) avec les données du DTO.

  - Pour les fichiers contenant les cas (comme `covid_19_clean_complete.csv`), le Mapper crée un nouvel objet `DiseaseCase` et le relie à la `Disease` ("COVID-19") et à la `Location` (Pays/Région/Comté) trouvées via le `CacheManager`.

c. **Étape 3 : Sauvegarde en Base de Données (Fait par `AbstractCsvImportService` et `PersistenceHelper`)**

- Une fois toutes les lignes du fichier traitées, le service demande au `PersistenceHelper` de sauvegarder.

- Le `PersistenceHelper` récupère tous les objets (Pays, Régions, Localisations, Cas) qui ont été créés ou modifiés et qui sont dans la mémoire du `CacheManager` ou accumulés par le service.

- Il envoie tout cela à la base de données en une seule fois (ou par gros paquets - "batch", dans le cas des données du fichier le plus volumineux : usa_county_wise.csv) pour être efficace.

- Grâce à l'annotation`@Transactional`, si une erreur survient pendant la sauvegarde d'un fichier, toutes les modifications pour ce fichier sont annulées, gardant la base de données cohérente.

3.  **Finalisation (Fait par `DataImportCovid19Runner`)**

- Une fois tous les fichiers traités, le Runner affiche un résumé : combien de lignes lues par fichier, combien de temps ça a pris, et combien d'objets (Pays, Régions, Cas, etc.) sont maintenant dans la base de données. Cela aide à faire des Benchmarks cohérentes.
