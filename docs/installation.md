# Guide d'Installation

## Prérequis Système

- Git
- Docker
- Docker Compose
- Compte Docker Hub (pour le mode production)

## Installation Pas à Pas

### 1. Récupération du Projet

```bash
git clone https://github.com/y2-znt/MSPR3.git
cd MSPR3
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

# Docker Hub (uniquement pour la production)
DOCKERHUB_USERNAME=your_dockerhub_username
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

#### Mode Développement

Le mode développement construit les images localement et permet le hot-reload :

```bash
# Construction et démarrage des services
docker compose up --build -d

# Vérification des services
docker compose ps
```

#### Mode Production

1. **Configuration SSL**

Avant de démarrer les services en production, vous devez générer des certificats SSL auto-signés :

```bash
# Création du dossier pour les certificats
mkdir -p certs

# Génération des certificats auto-signés
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout certs/nginx-selfsigned.key \
  -out certs/nginx-selfsigned.crt \
  -subj "/C=FR/ST=State/L=City/O=Organization/CN=localhost"
```

⚠️ Note : Ces certificats auto-signés sont destinés au développement local uniquement. Pour un environnement de production réel, utilisez des certificats SSL valides d'une autorité de certification.

2. **Démarrage des Services**

```bash
# Connexion à Docker Hub
docker login

# Récupération des images depuis Docker Hub
docker compose -f docker-compose.prod.yml pull

# Démarrage des services en production
docker compose -f docker-compose.prod.yml up -d
```

Pour vérifier que les services sont bien démarrés :

```bash
docker compose -f docker-compose.prod.yml ps
```

### Vérification de l'Installation

Une fois les services démarrés, vous pouvez accéder aux différentes interfaces :

**Mode Développement :**

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
# Connexion via Docker (Développement)
docker exec -it mspr3-db psql -U <username> -d <dbname>

# Connexion via Docker (Production)
docker exec -it mspr3-db-prod psql -U <username> -d <dbname>

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

# Tests Backend via container (Développement)
docker exec -it mspr3-backend mvn test

# Tests Frontend local
npm run test:e2e:ci

# Tests Frontend via container (Développement)
docker exec -it mspr3-frontend npm run test:e2e:ci
```

### Logs des Services

```bash
# Logs en temps réel (Développement)
docker compose logs -f

# Logs en temps réel (Production)
docker compose -f docker-compose.prod.yml logs -f

# Logs d'un service spécifique (Développement)
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db

# Logs d'un service spécifique (Production)
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f frontend
docker compose -f docker-compose.prod.yml logs -f db
```

## Gestion des Services

### Commandes Courantes

```bash
# Développement
# Arrêt des services
docker compose down

# Redémarrage d'un service spécifique
docker compose restart backend

# Suppression des volumes (reset des données)
docker compose down -v

# Production
# Arrêt des services
docker compose -f docker-compose.prod.yml down

# Redémarrage d'un service spécifique
docker compose -f docker-compose.prod.yml restart backend

# Suppression des volumes (reset des données)
docker compose -f docker-compose.prod.yml down -v
```

### Mise à Jour de l'Application

```bash
# Mise à jour depuis le repository
git pull

# Développement
docker compose down
docker compose build
docker compose up -d

# Production
docker compose -f docker-compose.prod.yml down
# Récupération des dernières images
docker compose -f docker-compose.prod.yml pull
# Redémarrage des services
docker compose -f docker-compose.prod.yml up -d
```

### Différences entre Développement et Production

Les principales différences entre les environnements de développement et de production sont :

1. **Configuration des conteneurs** : Les conteneurs de production utilisent des configurations optimisées pour la performance
2. **Noms des conteneurs** : Les conteneurs de production ont le suffixe `-prod`
3. **Volumes de données** : Les volumes de production sont séparés de ceux de développement
4. **Profils Spring** : Le backend utilise le profil `prod` en production
5. **Nginx** : Le frontend utilise Nginx en production pour servir l'application Angular
6. **SSL/HTTPS** : Le mode production utilise HTTPS avec des certificats SSL
