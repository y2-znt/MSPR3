# Modèle d'Intelligence Artificielle

## Aperçu

Ce document décrit le modèle d'intelligence artificielle utilisé pour la prédiction des cas de Covid-19. Le modèle est basé sur un algorithme de Random Forest et est servi via une API FastAPI.

## Détails du Modèle

- **Type de Modèle**: Random Forest
- **Fichier du Modèle**: `random_forest_model.pkl` (chargé via `joblib`)
- **Objectif**: Prédire le nombre de cas de Covid-19.

## Données d'Entrée (`CovidData`)

Le modèle attend les données d'entrée structurées comme suit:

| Champ            | Type    | Description                                                    |
| ---------------- | ------- | -------------------------------------------------------------- |
| `confirmed_case` | INTEGER | Nombre de cas confirmés existants                              |
| `date`           | DATE    | Date des données (non directement utilisée comme feature)      |
| `deaths`         | INTEGER | Nombre de décès existants                                      |
| `recovered`      | INTEGER | Nombre de personnes guéries existantes                         |
| `location`       | STRING  | Localisation spécifique (ex: "France - Île-de-France - Paris") |
| `region`         | STRING  | Région administrative (ex: "France - Île-de-France")           |
| `country`        | STRING  | Pays (ex: "France")                                            |
| `continent`      | STRING  | Continent                                                      |
| `population`     | FLOAT   | Population de la zone concernée                                |
| `who_region`     | STRING  | Région OMS                                                     |

## Traitement des Caractéristiques (Feature Engineering)

Les données d'entrée sont transformées en un vecteur de caractéristiques numériques avant d'être fournies au modèle.

### Caractéristiques Numériques Directes

Les champs suivants sont utilisés directement comme caractéristiques numériques :

- `confirmed_case`
- `deaths`
- `recovered`
- `population`

Total: 4 caractéristiques numériques.

### Encodage One-Hot pour les Caractéristiques Catégorielles

1.  **Continent (`continent`)**:

    - Encodé en un vecteur de 6 caractéristiques.
    - Mapping utilisé :
      ```python
      continents_map = {
          'AFRICA': 0,
          'ASIA': 1,
          'EUROPE': 2,
          'NORTH_AMERICA': 3,
          'OCEANIA': 4,
          'SOUTH_AMERICA': 5
      }
      ```

2.  **Région OMS (`who_region`)**:

    - Encodé en un vecteur de 6 caractéristiques.
    - Mapping utilisé :
      ```python
      who_regions_map = {
          'Africa': 0,
          'Americas': 1,
          'Eastern_Mediterranean': 2,
          'Europe': 3,
          'South-East_Asia': 4,
          'Western_Pacific': 5
      }
      ```

3.  **Localisation (`location`)**:

    - Encodé en un vecteur de 250 caractéristiques (taille fixe). La logique exacte de mapping n'est pas détaillée dans l'API mais est supposée être gérée par le modèle pré-entraîné.

4.  **Région (`region`)**:

    - Encodé en un vecteur de 250 caractéristiques (taille fixe). Similaire à `location`.

5.  **Pays (`country`)**:
    - Encodé en un vecteur de 211 caractéristiques (taille fixe). Similaire à `location`.

### Vecteur de Caractéristiques Final

Le vecteur final fourni au modèle est une concaténation de toutes ces caractéristiques :

- Caractéristiques numériques (4)
- Caractéristiques du continent (6)
- Caractéristiques de la région OMS (6)
- Caractéristiques de la localisation (250)
- Caractéristiques de la région (250)
- Caractéristiques du pays (211)

**Taille totale du vecteur de caractéristiques**: 4 + 6 + 6 + 250 + 250 + 211 = 727 caractéristiques.

## Sortie de Prédiction

L'API retourne les informations suivantes après une prédiction :

| Champ             | Type    | Description                                                                    |
| ----------------- | ------- | ------------------------------------------------------------------------------ |
| `prediction`      | INTEGER | Le nombre prédit de cas (arrondi à l'entier).                                  |
| `probability`     | FLOAT   | La probabilité associée à la classe prédite (max des probabilités par classe). |
| `features_length` | INTEGER | La longueur totale du vecteur de caractéristiques utilisé.                     |

## Points Importants

- Le champ `date` de l'objet `CovidData` n'est pas explicitement utilisé dans la construction du vecteur de caractéristiques dans le code de l'API `main.py`. Sa pertinence pour le modèle dépend de la manière dont le modèle `random_forest_model.pkl` a été entraîné.
- Les tailles fixes pour les vecteurs one-hot de `location`, `region`, et `country` (250, 250, 211 respectivement) suggèrent que le modèle a été entraîné avec un vocabulaire spécifique pour ces catégories. Toute nouvelle valeur non vue lors de l'entraînement pourrait ne pas être gérée de manière optimale ou entraîner une erreur si le mapping n'est pas présent.
- L'API inclut une gestion des erreurs basique et un logging des requêtes et des erreurs.
- La configuration CORS autorise les requêtes depuis `http://localhost:4200`.
