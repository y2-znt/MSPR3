# Architecture du Projet Backend

Ce document présente l'architecture générale du projet backend développé avec Spring Boot.

## Structure du Projet

```
backend/
├── src/                      # Code source principal
│   ├── main/                 # Code source de l'application
│   │   ├── java/            # Code Java
│   │   │   └── mspr/        # Package racine
│   │   │       ├── controllers/  # Contrôleurs REST
│   │   │       ├── services/     # Services métier
│   │   │       ├── repositories/ # Repositories JPA
│   │   │       ├── models/       # Entités et modèles
│   │   │       ├── config/       # Configuration Spring
│   │   │       └── dto/          # Objets de transfert de données
│   │   └── resources/       # Ressources et configuration
│   │       ├── application.properties  # Configuration principale
│   └── test/                # Tests unitaires et d'intégration
├── target/                  # Build de production
├── .mvn/                    # Configuration Maven Wrapper
└── pom.xml                  # Configuration Maven et dépendances

```

## Technologies et Frameworks

### Spring Boot

Le backend est construit avec Spring Boot 3.4.4, offrant :

- Un framework web robuste
- Une gestion simplifiée des dépendances
- Une configuration automatique
- Un serveur embarqué

### Base de données

- PostgreSQL comme système de gestion de base de données
- Spring Data JPA pour la persistance
- Hibernate comme ORM (Object-Relational Mapping)

### Documentation API

- SpringDoc OpenAPI UI (Swagger) pour la documentation des API REST
- Interface interactive accessible via l'endpoint `/swagger-ui.html`

## Organisation du Code

### Contrôleurs (controllers/)

Les contrôleurs REST gèrent les points d'entrée de l'API :

- Exposition des endpoints REST
- Validation des requêtes
- Gestion des réponses HTTP
- Documentation Swagger

### Services (services/)

La couche service contient la logique métier :

- Implémentation des règles métier
- Orchestration des opérations
- Transactions
- Validation des données

### Repositories (repositories/)

Interface avec la base de données :

- Requêtes JPA
- Requêtes personnalisées

### Modèles (entity/)

Entités JPA représentant la structure des données :

- Mapping objet-relationnel
- Validation des données
- Relations entre entités

### Configuration (config/)

Configuration de l'application :

- CORS

## Configuration et Environnement

### Maven

- `pom.xml` : Gestion des dépendances et du build
- Plugins configurés pour le développement et la production
- Dépendances principales :
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - postgresql
  - springdoc-openapi-ui

### Variables d'Environnement

- `.env` : Configuration des variables d'environnement
- Paramètres sensibles : Connexion base de données

## Docker

Le projet inclut une configuration Docker pour le déploiement :

- `backend.dockerfile` pour la construction de l'image
- `.dockerignore` pour exclure les fichiers non nécessaires

## Tests

Organisation des tests dans `src/test/` :

- Tests unitaires
- Tests d'intégration
- Configuration des tests
- Mocks et fixtures
