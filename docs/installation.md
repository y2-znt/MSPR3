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

## Arrêt des conteneurs

Pour arrêter les conteneurs :

```bash
docker compose down
```
