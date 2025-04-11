# Brief du Projet : Plateforme OMS de Suivi des Pandémies

## Description Générale

L'Organisation Mondiale de la Santé souhaite mettre en place une plateforme de veille pandémique. Cette solution devra permettre d'exploiter des jeux de données ouverts (open data), les transformer, les visualiser via une interface claire, et exposer les données via une API REST sécurisée.

En tant qu’équipe de l’entreprise ANALYZE IT, vous avez été mandatés pour réaliser ce projet de A à Z, en utilisant une stack moderne adaptée à une architecture web robuste.

## Objectifs Clés

- Intégrer et transformer les données en provenance de deux sources open data.
- Stocker les données nettoyées dans une base relationnelle PostgreSQL.
- Offrir une API REST documentée et filtrable pour les développeurs de l’OMS.
- Proposer des tableaux de bord interactifs affichés via une interface Angular.
- Industrialiser la solution avec Docker et GitLab CI/CD.
- Suivre le projet de manière agile, avec une planification continue.

## Fonctionnalités Principales

### 1. Collecte & Préparation des Données

- Sélection de **jeu de données Kaggle** (ex. : COVID-19).
- Format de données : JSON, CSV.
- Développement d’un module Spring pour effectuer le pipeline ETL :
  - Extraction automatique.
  - Nettoyage, suppression des doublons, normalisation des valeurs.
  - Chargement vers la base PostgreSQL via JPA.
- Documentation technique dédiée à l’ETL.

### 2. Modélisation et Stockage

- Modèle de données présenté en Merise et UML (MCD, MLD, MPD).
- Génération des entités JPA à partir du modèle logique.
- Structure optimisée pour les requêtes analytiques.

### 3. API RESTful

- Développée en Java avec Spring.
- Expose des endpoints CRUD pour les entités principales.
- Possibilité de filtrer les réponses (query params).
- Documentation complète via **Swagger (OpenAPI)**.

### 4. Visualisation des Données

- Frontend Angular affichant des tableaux de bord dynamiques :
  - KPI : taux de transmission, mortalité, zones géographiques touchées.
  - Graphiques filtrables par pays, période, maladie, etc.
- Justification des filtres mis en place dans la documentation.

### 5. Déploiement et Industrialisation

- Conteneurisation via **Docker** (frontend + backend + base de données).
- Automatisation CI/CD avec **GitLab** pour build/test/deploy.
- Pipeline intégrant les tests JUnit5 avant toute mise en production.

### 6. Documentation & Suivi

- Documentation Swagger pour l’API.
- Rapport technique sur l’ETL et le processus de nettoyage.
- Benchmark des outils d’analyse (ex : Apache Hop vs Talend).
- Suivi d’avancement régulier (Discord, Trello).
