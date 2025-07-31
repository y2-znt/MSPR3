# Documentation des logs IA

## 1. Emplacement et structure des logs

Les logs liés à l'IA sont stockés dans le dossier :

```
ai-api/logs/
```

Ce dossier contient :
- Les logs quotidiens des requêtes API : `api_requests_YYYY-MM-DD.csv`
- Les logs d'entraînement du modèle : `training.log`
- Un script d'analyse : `analyze_daily_logs.py`

### Mapping Docker
Dans l'environnement Docker, ce dossier est mappé pour persister les logs sur l'hôte :
```yaml
volumes:
  - ./ai-api/logs:/code/logs
```

### Rotation automatique
Un mécanisme de rotation supprime automatiquement les fichiers de logs API de plus de 30 jours.

---

## 2. Types de logs générés

### a) Logs de requêtes API (`api_requests_YYYY-MM-DD.csv`)
- Un fichier CSV est généré chaque jour.
- Chaque ligne correspond à une requête reçue par l'API IA.
- Les logs sont encodés en UTF-8.

**Colonnes :**
- `timestamp` : Date et heure de la requête
- `request_id` : Identifiant unique de la requête
- `endpoint` : Endpoint appelé (ex: `/predict`)
- `method` : Méthode HTTP
- `input_data` : Données d'entrée (JSON sérialisé)
- `prediction` : Résultat de la prédiction (si applicable)
- `probability` : Probabilité associée à la prédiction
- `features_used` : Nombre de features utilisées
- `status_code` : Code HTTP de la réponse
- `response_time_ms` : Temps de réponse en millisecondes
- `error_message` : Message d'erreur éventuel

**Exemple de ligne :**
```
2025-07-01T07:48:55.713275,2be1f70c-bd3c-4f4d-b1b6-f29dae551708,/predict,POST,"{""confirmed_case"": 6, ...}",0,0.2047,727,200,8.55,
```

### b) Logs d'entraînement (`training.log`)
- Générés lors de l'entraînement du modèle (notebook ou script Python).
- Contiennent les étapes, paramètres, durées, et résultats de l'entraînement.
- Encodage recommandé : UTF-8 (à spécifier lors de la création du FileHandler Python).

**Exemple de contenu :**
```
2025-07-01 10:36:19,069 - model_training - INFO - === DÉBUT DE L'ENTRAÎNEMENT DU MODÈLE RANDOM FOREST ===
2025-07-01 10:36:19,087 - model_training - INFO - Nombre d'échantillons d'entraînement: 355508
2025-07-01 10:36:19,094 - model_training - INFO - Nombre de features: 4113
2025-07-01 10:36:19,103 - model_training - INFO - Paramètres du modèle: {'n_estimators': 10, 'max_depth': 6, 'random_state': 42}
2025-07-01 10:36:19,137 - model_training - INFO - Début de l'entraînement...
2025-07-01 10:44:19,149 - model_training - INFO - Entraînement terminé en 480.01 secondes
2025-07-01 10:44:19,150 - model_training - INFO - Précision sur le jeu de développement: 0.2396
```

---

## 3. Génération des logs

### a) Dans l'API FastAPI
- Les logs de requêtes sont générés automatiquement à chaque appel d'endpoint.
- Un middleware et des fonctions utilitaires écrivent les logs dans le CSV quotidien.
- Les erreurs sont également loggées avec le message d'erreur et le code HTTP.

### b) Dans le notebook ou script d'entraînement
- Le logger Python (`logging.getLogger`) est configuré pour écrire dans `training.log`.
- Il est recommandé d'utiliser :
  ```python
  logging.FileHandler('training.log', encoding='utf-8')
  ```
- Les étapes importantes (démarrage, paramètres, résultats, erreurs) sont loggées.

---

## 4. Lecture et analyse des logs

### a) Analyse des logs API
- Utiliser le script fourni :
  ```bash
  cd ai-api/logs
  python analyze_daily_logs.py
  ```
- Ce script affiche :
  - Nombre total de requêtes
  - Répartition par endpoint
  - Statistiques de temps de réponse
  - Répartition des codes de statut
  - Erreurs principales

### b) Lecture des logs d'entraînement
- Ouvrir `ai-api/logs/training.log` dans un éditeur compatible UTF-8.
- Rechercher les sections "DÉBUT DE L'ENTRAÎNEMENT" et "Entraînement terminé" pour voir la durée et la précision obtenue.

---

## 5. Bonnes pratiques

- Toujours forcer l'encodage UTF-8 lors de l'écriture de logs (surtout sous Windows).
- Surveiller la taille des fichiers de logs et activer la rotation/suppression automatique.
- Ne jamais stocker d'informations sensibles dans les logs.
- Versionner les scripts d'analyse et la configuration du logging.

---

**Pour toute question ou amélioration, voir le README du dossier `ai-api/logs/` ou contacter l'équipe IA.** 