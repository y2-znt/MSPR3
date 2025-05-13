# Guide d'Installation

## Prérequis Système

- Git
- Docker
- Docker Compose

## Installation Pas à Pas

### 1. Récupération du Projet

```bash
git clone https://github.com/y2-znt/MSPR2.git
cd MSPR2
```

### 2. Configuration de l'Environnement

#### Configuration Globale

Créez un fichier `.env` à la racine du projet :

```bash
# Configuration PostgreSQL
DB_URL=jdbc:postgresql://db:5432/database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_PORT=5432
DB_NAME=database_name
```

#### Configuration Backend

Créez un fichier `.env` dans le dossier `backend` :

```bash
# Configuration Base de données
DB_URL=jdbc:postgresql://db:5432/database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### 3. Démarrage de l'Application

```bash
# Construction des images
docker compose build

# Démarrage des services
docker compose up -d
```

Vérifiez que les services sont bien démarrés :

```bash
docker compose ps
```

### 4. Vérification de l'Installation

Une fois les services démarrés, vous pouvez accéder aux différentes interfaces :

| Service     | URL                            | Description                   |
| ----------- | ------------------------------ | ----------------------------- |
| Frontend    | http://localhost:4200          | Interface utilisateur Angular |
| Backend API | http://localhost:8080          | API REST Spring Boot          |
| Swagger UI  | http://localhost:8080/api-docs | Documentation API Spring Boot |
| FastAPI     | http://localhost:8000          | API IA                        |
| Swagger UI  | http://localhost:8000/docs     | Documentation API FastAPI     |
| PostgreSQL  | localhost:5432                 | Base de données               |

## Gestion de la Base de Données

### Connexion à PostgreSQL

```bash
# Connexion via Docker
docker exec -it mspr2-db psql -U <username> -d <dbname>

# Commandes PostgreSQL utiles
\dt                 # Liste des tables
\d <nom_table>      # Structure d'une table
\l                  # Liste des bases de données
\du                 # Liste des utilisateurs
```

## Tests et Validation

### Exécution des Tests

```bash
# Tests Backend local
mvn test # ou ./mvnw test

# Tests Backend via container
docker exec -it mspr2-backend mvn test

# Tests Frontend local
npm run test:e2e:ci

# Tests Frontend via container
docker exec -it mspr2-frontend npm run test:e2e:ci
```

### Logs des Services

```bash
# Logs en temps réel
docker compose logs -f

# Logs d'un service spécifique
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
```

## Gestion des Services

### Commandes Courantes

```bash
# Arrêt des services
docker compose down

# Redémarrage d'un service spécifique
docker compose restart backend

# Suppression des volumes (reset des données)
docker compose down -v
```

### Mise à Jour de l'Application

```bash
# Mise à jour depuis le repository
git pull

# Reconstruction et redémarrage
docker compose down
docker compose build
docker compose up -d
```
