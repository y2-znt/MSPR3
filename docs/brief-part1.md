# Brief du Projet : Plateforme OMS de Suivi des Pandémies

## Contexte Détaillé

Dans le cadre de la gestion de la pandémie de COVID-19 et de la montée du virus de la variole du singe (MPOX), l'Europe et le gouvernement américain ont décidé de créer une nouvelle division de recherche au sein de l'OMS. Cette division est dédiée à la détection et à la prévention des pandémies. L'objectif est de développer une plateforme permettant de collecter, nettoyer, analyser et visualiser les données historiques sur les pandémies. Cette plateforme doit aider les chercheurs et les décideurs à visualiser des indicateurs clés, comprendre les dynamiques des pandémies passées et formuler des hypothèses pour des modèles prédictifs. Actuellement, la division ne compte qu'une seule personne concentrée sur la mise en place de l'infrastructure, nécessitant donc l'intervention de prestataires pour développer la solution.

## Périmètre de la MSPR

Pour cette Mise en Situation Professionnelle Reconstituée (MSPR), vous travaillez pour le groupe ANALYZE IT, spécialisé dans les données de santé, et dépêché par l'OMS en tant que prestataire de service. Votre mission est de développer une solution permettant d'ingérer des données provenant de différentes sources (bases de données publiques de santé, archives hospitalières, publications scientifiques, etc.) au format JSON ou CSV. Vous devez nettoyer, trier et assurer la qualité des données, en tenant compte des sources hétérogènes. Vous devez également construire une structure de stockage adaptée pour l'analyse des données, permettant une interrogation rapide et flexible. Enfin, vous devez mettre en place une solution de lecture de données pour l'équipe de développement de la division et créer des tableaux de bord interactifs pour la visualisation et l'extraction des indicateurs clés de performance.

## Description Générale

L'Organisation Mondiale de la Santé souhaite mettre en place une plateforme de veille pandémique. Cette solution devra permettre d'exploiter des jeux de données ouverts (open data), les transformer, les visualiser via une interface claire, et exposer les données via une API REST sécurisée.

En tant qu'équipe de l'entreprise ANALYZE IT, vous avez été mandatés pour réaliser ce projet de A à Z, en utilisant une stack moderne adaptée à une architecture web robuste.

## Objectifs Clés

- Intégrer et transformer les données en provenance de deux sources open data.
- Stocker les données nettoyées dans une base relationnelle PostgreSQL.
- Offrir une API REST documentée et filtrable pour les développeurs de l'OMS.
- Proposer des tableaux de bord interactifs affichés via une interface Angular.
- Industrialiser la solution avec Docker et GitLab CI/CD.
- Suivre le projet de manière agile, avec une planification continue.

## Fonctionnalités Principales

### 1. Collecte & Préparation des Données

- Sélection de **jeu de données Kaggle** (ex. : COVID-19).
- Format de données : JSON, CSV.
- Développement d'un module Spring pour effectuer le pipeline ETL :
  - Extraction automatique.
  - Nettoyage, suppression des doublons, normalisation des valeurs.
  - Chargement vers la base PostgreSQL via JPA.
- Documentation technique dédiée à l'ETL.

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

- Documentation Swagger pour l'API.
- Rapport technique sur l'ETL et le processus de nettoyage.
- Benchmark des outils d'analyse (ex : Apache Hop vs Talend).
- Suivi d'avancement régulier (Discord, Trello).
