# Architecture du Projet Frontend

Ce document présente l'architecture générale du projet frontend développé avec Angular.

## Structure du Projet

```
frontend/
├── src/                     # Code source principal
│   ├── app/                 # Composants et logique de l'application
│   │   ├── components/      # Composants réutilisables
│   │   ├── services/        # Services pour la logique métier et API
│   │   ├── models/         # Interfaces et modèles de données
│   │   ├── pipes/          # Pipes personnalisés
│   │   └── app.routes.ts   # Configuration du routage
│   ├── environments/       # Configuration des environnements
│   ├── assets/            # Ressources statiques
│   └── styles.scss        # Styles globaux
├── public/                # Fichiers publics statiques
├── dist/                  # Build de production
└── node_modules/         # Dépendances
```

## Organisation du Code

### Composants

Les composants sont organisés dans le dossier `src/app/components/`. Chaque composant suit la structure standard Angular avec :

- Un fichier `.ts` pour la logique
- Un fichier `.html` pour le template
- Un fichier `.scss` pour les styles
- Un fichier `.spec.ts` pour les tests

### Services

### Services (src/app/services/)

Les services constituent la couche d'accès aux données et la logique métier de l'application. Ils suivent le pattern Injectable d'Angular et sont organisés comme suit :

#### Communication API

- Utilisation du `HttpClient` d'Angular pour les requêtes HTTP
- Gestion des appels REST API avec observables (RxJS)
- Services principaux :
  - `DiseaseService` : Gestion des maladies
  - `CountryService` : Gestion des pays avec pagination
  - `RegionService` : Gestion des régions
  - `LocationService` : Gestion des localisations
  - `DiseaseCaseService` : Gestion des cas de maladies

### Modèles

Le dossier `src/app/models/` contient les interfaces TypeScript définissant la structure des données.

## Configuration et Environnement

### TypeScript

- `tsconfig.json` : Configuration stricte de TypeScript
- Types stricts pour une meilleure maintenabilité
- Déclarations de types personnalisés

### Angular Configuration

- `angular.json` : Configuration du workspace Angular
- Optimisations de build pour la production
- Configuration des assets et styles globaux

### Variables d'Environnement

- `.env` : Variables d'environnement sensibles
- `environments/*.ts` : Configuration spécifique par environnement
  - URLs des API
  - Clés d'API
  - Configurations spécifiques

## Docker

Le projet inclut une configuration Docker pour le déploiement avec :

- `frontend.dockerfile` pour la construction de l'image
- `.dockerignore` pour exclure les fichiers non nécessaires
