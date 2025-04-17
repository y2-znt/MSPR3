# Instructions d'installation

## Prérequis

- Git
- Docker
- Docker Compose

## Étapes d'installation

1. Cloner le repository

```bash
git clone <url_du_repo>
cd mspr-1
```

2. Configuration des fichiers d'environnement

   - Dans le dossier racine (mspr-1), créer un fichier `.env` basé sur `.env.example`
   - Dans le dossier `backend`, créer un fichier `.env` basé sur `backend/.env.example`
   - Personnaliser les valeurs dans ces fichiers selon vos besoins (facultatif)

3. Construction et démarrage des conteneurs

```bash
docker compose build
docker compose up -d
```

## Accès à la base de données PostgreSQL

Pour se connecter à la base de données PostgreSQL via le terminal :

```bash
docker exec -it mspr1-db psql -U <username> -d <dbname>
```

Remplacez `<username>` et `<dbname>` par les valeurs configurées dans votre fichier `.env`.

Une fois connecté, vous pouvez :

- Voir toutes les tables :

```sql
\dt
```

- Voir la structure d'une table spécifique (remplacez `nom_table` par le nom de la table) :

```sql
\d nom_table
```

## Documentation API avec Swagger UI

Une fois l'application démarrée, vous pouvez accéder à la documentation interactive de l'API via Swagger UI :

```
http://localhost:8080/api-docs
```

Cette interface vous permet de :

- Explorer tous les endpoints disponibles
- Tester les requêtes directement depuis le navigateur
- Voir les modèles de données et les schémas
- Comprendre les paramètres requis pour chaque endpoint

## Exécution des tests

Pour lancer les tests depuis le conteneur backend :

```bash
docker exec -it mspr1-backend mvn test
```

Cette commande exécutera tous les tests JUnit 5 définis dans le projet.

## Arrêt des conteneurs

Pour arrêter les conteneurs :

```bash
docker compose down
```
