# Système de Health Check et Logging de l'API AI

Ce document décrit en détail le fonctionnement du système de health check et de logging implémenté dans l'API AI du projet MSPR3.

## Table des matières
1. [Configuration du logging](#configuration-du-logging)
2. [Health Check Endpoint](#health-check-endpoint)
3. [Health Check Planifié](#health-check-planifié)
4. [Configuration Docker](#configuration-docker)
5. [Bonnes pratiques et recommandations](#bonnes-pratiques-et-recommandations)

## Configuration du logging

Le système de logging est configuré au début de l'application pour assurer une traçabilité complète de son fonctionnement:

```python
# Configuration des logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
```

Cette configuration initialise le logger avec un niveau INFO, ce qui permet de capturer les événements importants tout en filtrant les détails de débogage trop verbeux.

Les logs sont utilisés à des moments clés dans l'application:

1. **Au chargement du modèle**:
   ```python
   logger.info(f"Trying to load model from: {model_path}")
   logger.info("Model loaded successfully")
   # ou en cas d'erreur:
   logger.error(f"Could not load model from {model_path}: {str(e)}")
   ```

2. **Lors de l'initialisation de l'API**:
   ```python
   logger.info(f"CORS allowed origins: {allowed_origins}")
   ```

3. **Pendant le traitement des requêtes de prédiction**:
   ```python
   logger.info("Réception d'une nouvelle requête de prédiction")
   logger.info(f"Prédiction effectuée avec succès: {prediction}")
   # ou en cas d'erreur:
   logger.error(f"Erreur lors de la prédiction: {str(e)}")
   ```

4. **Lors des health checks**:
   ```python
   logger.error(f"Health check failed: {str(e)}")
   ```

## Health Check Endpoint

L'API expose un endpoint `/health` qui permet de vérifier son état de santé:

```python
@app.get("/health")
def health_check():
    try:
        if model is None:
            raise ValueError("Le modèle n'est pas chargé")
        dummy_input = np.zeros((1, 4 + 6 + 6 + 250 + 250 + 211))
        _ = model.predict(dummy_input)
        return {"status": "ok", "model": "loaded", "prediction": "ok"}
    except Exception as e:
        logger.error(f"Health check failed: {str(e)}")
        return {"status": "error", "detail": str(e)}
```

Ce endpoint effectue trois vérifications essentielles:
1. **Vérification de l'existence du modèle**: S'assure que le modèle a été correctement chargé
2. **Vérification de la capacité de prédiction**: Exécute une prédiction test pour s'assurer que le modèle fonctionne
3. **Retour d'un statut**: Renvoie un JSON contenant l'état du service

Les réponses possibles sont:
- En cas de succès: `{"status": "ok", "model": "loaded", "prediction": "ok"}`
- En cas d'échec: `{"status": "error", "detail": "<message d'erreur>"}`

## Health Check Planifié

En plus de l'endpoint accessible à la demande, l'API effectue des vérifications de santé automatiques et périodiques:

```python
def scheduled_health_check():
    logger.info("Running scheduled health check")
    try:
        # En production (dans Docker), l'API écoute sur le port 80
        # En développement, elle écoute sur le port 8000
        # Utilisation d'une variable d'environnement avec fallback intelligent
        health_url = os.getenv("HEALTH_CHECK_URL", "http://localhost")
        
        # Si port n'est pas spécifié et qu'on est en développement, utiliser 8000
        if ":" not in health_url and health_url == "http://localhost":
            health_url += ":8000"
            
        # Ajout du chemin /health
        if not health_url.endswith("/health"):
            health_url += "/health"
            
        logger.info(f"Checking health at: {health_url}")
        resp = requests.get(health_url)
        
        if resp.status_code == 200:
            logger.info("Scheduled health check success: %s", resp.json())
        else:
            logger.warning(f"Scheduled health check failed with status {resp.status_code}")
    except Exception as e:
        logger.error(f"Scheduled health check exception: {e}")
```

Cette fonction est exécutée à intervalles réguliers grâce à APScheduler:

```python
scheduler = BackgroundScheduler()
scheduler.add_job(scheduled_health_check, "interval", seconds=10)
scheduler.start()

atexit.register(lambda: scheduler.shutdown())
```

Caractéristiques du health check planifié:
- **Fréquence**: Exécuté toutes les 10 secondes
- **Adaptabilité**: S'adapte automatiquement à l'environnement (dev/prod) pour déterminer la bonne URL
- **Journalisation**: Enregistre chaque vérification avec son résultat
- **Arrêt propre**: Le scheduler est automatiquement arrêté à la fin de l'application

## Configuration Docker

La configuration Docker pour l'API AI inclut un health check au niveau du conteneur:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost/health"]
  interval: 30s
  timeout: 5s
  retries: 3
```

Cette configuration:
1. **Vérifie l'endpoint `/health`**: Utilise curl pour vérifier que l'endpoint répond correctement
2. **Intervalle de 30 secondes**: Effectue la vérification toutes les 30 secondes
3. **Timeout de 5 secondes**: Considère le test comme échoué si la réponse prend plus de 5 secondes
4. **3 tentatives**: Tente la vérification jusqu'à 3 fois avant de marquer le conteneur comme "unhealthy"

Docker utilise ces informations pour:
- Déterminer l'état du conteneur (healthy, unhealthy, ou starting)
- Prendre des décisions de redémarrage si nécessaire
- Informer d'autres services qui dépendent de ce conteneur

## Bonnes pratiques et recommandations

Pour maintenir et améliorer le système de health check et de logging:

1. **Niveaux de logs adaptés**:
   - Utiliser `logger.info()` pour les opérations normales importantes
   - Utiliser `logger.warning()` pour les situations anormales mais non critiques
   - Utiliser `logger.error()` pour les problèmes empêchant une fonctionnalité de s'exécuter
   - Utiliser `logger.debug()` pour les détails techniques utiles au débogage uniquement

2. **Variables d'environnement**:
   - `HEALTH_CHECK_URL`: Permet de configurer l'URL du health check en fonction de l'environnement
   - `CORS_ALLOWED_ORIGINS`: Configure les origines autorisées pour les requêtes CORS

3. **Surveillance et alertes**:
   - Configurer des alertes basées sur les logs (erreurs répétées)
   - Mettre en place un système de surveillance qui vérifie l'endpoint `/health`

4. **Extension possible**:
   - Ajouter des métriques de performance au health check
   - Implémenter des vérifications plus spécifiques (charge mémoire, CPU, etc.)
   - Créer un dashboard de monitoring pour visualiser l'état du système

En suivant ces recommandations, le système de santé de l'API AI restera robuste et facile à maintenir, assurant une haute disponibilité et une détection rapide des problèmes.
