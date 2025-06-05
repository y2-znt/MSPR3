<div align="center">

# Plateforme de Veille Pandémique OMS

![Angular](https://img.shields.io/badge/angular-%23DD0031.svg?style=for-the-badge&logo=angular&logoColor=white)
![TypeScript](https://img.shields.io/badge/typescript-%23007ACC.svg?style=for-the-badge&logo=typescript&logoColor=white)
![SASS](https://img.shields.io/badge/SASS-hotpink.svg?style=for-the-badge&logo=SASS&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0056B3?style=for-the-badge&logo=fastapi&logoColor=white)
![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)
![Jupyter](https://img.shields.io/badge/Jupyter-F37626.svg?style=for-the-badge&logo=Jupyter&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=Swagger&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)

![Dernière mise à jour](https://img.shields.io/badge/Dernière%20mise%20à%20jour-2024-01-01-blue?style=for-the-badge)

</div>

Solution de surveillance et d'analyse épidémiologique développée pour l'Organisation Mondiale de la Santé, permettant la collecte, le traitement et la visualisation des données pandémiques à l'échelle mondiale.

> _⚠️ Ce projet est actuellement en phase de développement. La mise en production est prévue prochainement. En attendant, l'application peut être lancée localement via Docker._

## Caractéristiques

- Traitement automatisé des données via ETL personnalisé
- API REST sécurisée avec documentation Swagger
- Interface utilisateur interactive développée avec Angular Material
- Visualisations avancées des données avec Chart.js
- Accessibilité et conformité aux normes WCAG
- Prédictions de l'évolution pandémique via un modèle d'IA
- Déploiement conteneurisé et pipeline CI/CD
- Tests automatisés et intégration continue
- **Changelog automatique** basé sur les commits conventionnels

## Démarrage Rapide

```bash
# Cloner le repository
git clone https://github.com/y2-znt/MSPR3.git
cd MSPR3

# Lancer l'application avec Docker
docker-compose up -d
```

Consultez le [guide d'installation](docs/installation.md) pour une configuration détaillée.

## 📋 Changelog

Les changements du projet sont automatiquement documentés dans le [CHANGELOG.md](CHANGELOG.md). 

Pour consulter l'historique des versions et les nouveautés :
- 📋 [Voir le changelog complet](CHANGELOG.md)
- 🏷️ [Releases GitHub](https://github.com/y2-znt/MSPR3/releases)

## Stack Technique

| Composant       | Technologies                                               |
| --------------- | ---------------------------------------------------------- |
| Frontend        | Angular 18, TypeScript, Material UI, Chart.js              |
| Backend         | Spring Boot, Java 21, JPA, Swagger                         |
| API IA          | FastAPI, Python 3.12, Scikit-learn, Pandas, NumPy          |
| Traitement IA   | Jupyter Notebook, Python 3.12, Scikit-learn, Pandas, NumPy |
| Base de données | PostgreSQL                                                 |
| Infrastructure  | Docker, Docker Compose, GitHub Actions                     |

## Documentation

### Projet

- [Présentation Part 1](docs/presentation_part1.md)
- [Présentation Part 2](docs/presentation_part2.md)
- [Installation](docs/installation.md)
- [Workflow Git](docs/git-workflow.md)
- [Gestion de projet](docs/project-management.md)
- [📋 Changelog automatique](docs/changelog-guide.md)

### Technique

- [Architecture](docs/architecture/)
- [Modèle de données](docs/data-model.md)
- [Modèle IA](docs/ai-model.md)
- [Processus ETL](docs/etl.md)
- [Technologies utilisées](docs/tech-stack.md)

### Benchmarks

- [Benchmark Backend](docs/benchmark/benchmark-backend.md)
- [Benchmark Frontend](docs/benchmark/benchmark-frontend.md)
- [Benchmark ETL](docs/benchmark/benchmark-etl.md)
- [Benchmark Entrainement IA](docs/benchmark/benchmark-ai-training.md)
- [Benchmark API IA](docs/benchmark/benchmark-ai-api.md)

### Diagrammes

- [Diagrammes de cas d'utilisation](docs/ressources/diagrams/use-case/use-case-diagram.png)
- [Diagrammes de séquence](docs/ressources/diagrams/sequence)
- [Diagrammes de classe](docs/ressources/diagrams/class)
- [Merise](docs/ressources/diagrams/merise)

---

Développé par l'équipe MSPR-2 d'ANALYZE IT.
