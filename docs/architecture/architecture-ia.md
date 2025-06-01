# Architecture du Projet API IA

Ce document présente l'architecture générale du projet API IA développé avec FastAPI.

## Structure du Projet

```
ai-api/
├── app/                     # Code source principal de l'application IA
│   ├── model/               # Modèles de Machine Learning et preprocesseurs
│   ├── main.py              # Point d'entrée de l'API FastAPI et logique des routes
│   └── __init__.py          # Initialisation du module app
├── requirements.txt         # Dépendances Python du projet
├── ai.dockerfile            # Configuration Docker pour l'API IA
└── .gitignore               # Fichiers et dossiers à ignorer par Git
```

## Organisation du Code

### `main.py`

Ce fichier est le cœur de l'API IA. Il est responsable de :

- L'initialisation de l'application FastAPI.
- La définition des routes (endpoints) de l'API.
- Le chargement des modèles de Machine Learning au démarrage (si applicable).
- La gestion des requêtes entrantes, incluant la validation des données.
- L'appel aux fonctions de prédiction des modèles.
- La formulation et le renvoi des réponses HTTP.

### `model/`

Ce répertoire contient les éléments liés aux modèles de Machine Learning :

- Les fichiers des modèles de Machine Learning entraînés (par exemple, au format `.joblib` ou `.pkl`).
- Les scripts ou objets de prétraitement des données (scalers, encodeurs, etc.) nécessaires avant de faire des prédictions.
- Potentiellement, des scripts d'entraînement ou de réentraînement des modèles (bien que l'entraînement puisse être géré dans un processus séparé).

## Technologies et Frameworks

### FastAPI

L'API est construite avec FastAPI, un framework web moderne et performant pour Python, offrant :

- Une validation automatique des données basée sur les types Python.
- Une documentation interactive automatique des API (Swagger UI et ReDoc).
- Des performances élevées grâce à Starlette et Pydantic.
- La prise en charge de la programmation asynchrone.

### Uvicorn

Uvicorn est utilisé comme serveur ASGI (Asynchronous Server Gateway Interface) pour exécuter l'application FastAPI.

### Scikit-learn, Pandas, NumPy

Ces bibliothèques sont utilisées pour :

- `scikit-learn` : Charger et utiliser les modèles de Machine Learning.
- `pandas` : Manipuler et structurer les données d'entrée pour les modèles.
- `numpy` : Effectuer des opérations numériques, souvent en conjonction avec `pandas` et `scikit-learn`.

### Joblib

Utilisé pour la sérialisation et la désérialisation des modèles `scikit-learn` (sauvegarde et chargement des modèles).

## Configuration et Environnement

### `requirements.txt`

Ce fichier liste toutes les dépendances Python nécessaires pour faire fonctionner l'API IA. Il est utilisé pour recréer l'environnement d'exécution de manière cohérente.

### Variables d'Environnement

Bien que non explicitement listé dans la structure de base, il est courant d'utiliser des variables d'environnement pour configurer certains aspects de l'application, tels que :

- Les chemins vers les fichiers de modèles.
- Les configurations spécifiques à l'environnement (développement, production).
- Les clés d'API ou autres informations sensibles.

## Docker

Le projet inclut une configuration Docker pour le déploiement de l'API IA :

- `ai.dockerfile` : Contient les instructions pour construire l'image Docker de l'application. Cela inclut l'installation des dépendances Python et la copie du code source de l'application.
- `.dockerignore` : Spécifie les fichiers et dossiers à exclure du contexte de build Docker pour optimiser la taille de l'image et la vitesse de construction.

## Flux de Prédiction Typique

1. Une requête HTTP (généralement POST) arrive sur un endpoint défini dans `main.py`.
2. FastAPI valide les données d'entrée par rapport au schéma Pydantic défini.
3. Les données d'entrée sont prétraitées si nécessaire (en utilisant des objets chargés depuis le dossier `model/`).
4. Les données prétraitées sont passées au modèle de Machine Learning chargé (depuis `model/`) pour obtenir une prédiction.
5. La prédiction est formatée et renvoyée dans une réponse HTTP.
