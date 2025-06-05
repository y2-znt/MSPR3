# Changelog - Plateforme OMS de Suivi des Pandémies

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Non publié]

- feat: implement pagination for countries table ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- feat: add health check endpoint to Nginx configuration ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- feat: add Spring Boot Actuator dependencies and configure health endpoint ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- feat: update translations for COVID-19 dashboard with parameterized labels and improved structure ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- feat: enhance translation service to support parameter interpolation in instant translations ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- feat: enhance dashboard and dialog components with improved translations and formatting ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- feat: enhance translation service and app component for improved internationalization ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- feat: implement health check endpoint and scheduled health checks with logging ([commit](https://github.com/y2-znt/MSPR3/commit/hash))

- Mise en place de la pipeline CI/CD complète
- Configuration des tests automatisés
- Déploiement automatique des images Docker

- fix: update health check URLs to use localhost ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- fix: update health check URLs to use SERVER_NAME variable ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- fix: update location and region models to support multiple associations ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- fix: update requirements.txt to specify APScheduler version and clean up imports in main.py ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- fix: update Nginx configuration to enable HTTP/2 support ([commit](https://github.com/y2-znt/MSPR3/commit/hash))

- Configuration initiale du projet
- Mise en place de l'architecture multi-services
- Configuration de GitHub Actions

- docs: update changelog ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- refactor: remove health check URL and related code ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: uncomment deployment conditions in GitHub Actions workflow ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: enhance Docker configuration with resource limits and health checks ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: update GitHub Actions workflow to use specific Docker image versions (1.0.0 for backend, frontend, and ai-api; 17 for PostgreSQL) and comment out deployment conditions ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: update Docker images in docker-compose.prod.yml to specific versions (1.0.0 for frontend, backend, and ai-api; 17 for PostgreSQL) ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: update PostgreSQL image version in docker-compose.yml to 17 ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- docs: rename Documentation.yml to documentation.yml and fix name file ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- docs: add project brief for Phase 3 of OMS pandemic tracking platform ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- test: refactor dashboard component tests for improved readability and consistency ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: add health check URL to .env.example and installation documentation; update scheduled health check interval to 15 minutes ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: update production Docker configuration by adding NODE_ENV variable, adjusting volume mappings, and removing unnecessary dependencies ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: remove example .env file and related configuration instructions from installation documentation ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: remove dotenv-java dependency and related .env loading logic from BackendApplication ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: update Docker configurations for development environment, including volume mappings, environment variables, and restart policies ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: remove DB_PORT from configuration files and delete obsolete GitLab CI configuration ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
- chore: update .gitignore to include SSL certificate files and enhance installation documentation with SSL setup instructions ([commit](https://github.com/y2-znt/MSPR3/commit/hash))
