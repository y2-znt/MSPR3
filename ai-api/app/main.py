from fastapi import FastAPI, HTTPException, Request, Response
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from datetime import date, datetime, timedelta
import joblib
import numpy as np
import pandas as pd
import logging
import os
import time
import csv
import json
import uuid
import glob

# Configuration des logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Activer les logs de debug pour le développement
logger.setLevel(logging.DEBUG)

# Fonction pour nettoyer les anciens fichiers de logs (garde seulement 30 jours)
def cleanup_old_logs():
    """Supprime les fichiers de logs plus anciens que 30 jours"""
    logs_dir = './logs'
    if not os.path.exists(logs_dir):
        return
    
    cutoff_date = datetime.now() - timedelta(days=30)
    pattern = os.path.join(logs_dir, 'api_requests_*.csv')
    
    for log_file in glob.glob(pattern):
        try:
            # Extraire la date du nom de fichier
            filename = os.path.basename(log_file)
            date_str = filename.replace('api_requests_', '').replace('.csv', '')
            file_date = datetime.strptime(date_str, '%Y-%m-%d')
            
            if file_date < cutoff_date:
                os.remove(log_file)
                logger.info(f"Ancien fichier de log supprimé: {log_file}")
        except Exception as e:
            logger.error(f"Erreur lors de la suppression du fichier {log_file}: {e}")

# Fonction pour rendre les données sérialisables en JSON
def make_json_serializable(obj):
    """Convertit les objets non-sérialisables en JSON (comme date) en string"""
    if isinstance(obj, dict):
        return {key: make_json_serializable(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [make_json_serializable(item) for item in obj]
    elif isinstance(obj, (date, datetime)):
        return obj.isoformat()
    else:
        return obj

# Fonction pour logger les requêtes dans un CSV quotidien
def log_request_to_daily_csv(request_id, endpoint, method, input_data, prediction_result, status_code, response_time_ms, error_message=None):
    """Log les informations de requête dans un fichier CSV quotidien"""
    try:
        # Créer le dossier logs s'il n'existe pas  
        logs_dir = './logs'
        os.makedirs(logs_dir, exist_ok=True)
        
        # Nom du fichier avec la date du jour
        today = datetime.now().strftime('%Y-%m-%d')
        csv_file = os.path.join(logs_dir, f'api_requests_{today}.csv')
        
        # Vérifier le chemin absolu pour debug
        abs_csv_file = os.path.abspath(csv_file)
        
        # Créer le fichier CSV avec headers s'il n'existe pas
        if not os.path.exists(csv_file):
            logger.info(f"Création du nouveau fichier de log: {abs_csv_file}")
            with open(csv_file, 'w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow([
                    'timestamp', 'request_id', 'endpoint', 'method', 
                    'input_data', 'prediction', 'probability', 'features_used',
                    'status_code', 'response_time_ms', 'error_message'
                ])
        
        # Extraire les informations de prédiction
        prediction = ''
        probability = ''
        features_used = ''
        
        if prediction_result and isinstance(prediction_result, dict):
            prediction = prediction_result.get('prediction', '')
            probability = prediction_result.get('probability', '')
            features_used = prediction_result.get('features_length', '')
        
        # Préparer les données d'entrée pour la sérialisation JSON
        serializable_input_data = ''
        if input_data:
            try:
                # Rendre les données sérialisables (convertir date en string)
                serializable_data = make_json_serializable(input_data)
                serializable_input_data = json.dumps(serializable_data)
            except Exception as e:
                serializable_input_data = f"Error serializing: {str(e)}"
                logger.warning(f"Erreur de sérialisation des données d'entrée: {e}")
        
        # Ajouter la nouvelle ligne
        with open(csv_file, 'a', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow([
                datetime.now().isoformat(),
                request_id,
                endpoint,
                method,
                serializable_input_data,
                prediction,
                probability,
                features_used,
                status_code,
                response_time_ms,
                error_message or ''
            ])
            
        logger.debug(f"Ligne ajoutée au fichier CSV: {abs_csv_file}")
        
    except Exception as e:
        logger.error(f"Erreur fatale dans log_request_to_daily_csv: {str(e)}")
        raise

# Vérifier si le modèle existe
model_path = "app/model/random_forest_model.pkl"
if not os.path.exists(model_path):
    raise FileNotFoundError(f"Le modèle n'existe pas à l'emplacement {model_path}")

# Chargement du modèle
logger.info("Chargement du modèle...")
try:
    model = joblib.load(model_path)
    logger.info("Modèle chargé avec succès")
except Exception as e:
    logger.error(f"Erreur lors du chargement du modèle: {str(e)}")
    raise

app = FastAPI()

# Nettoyer les anciens logs au démarrage
cleanup_old_logs()

# Middleware pour logger toutes les requêtes
@app.middleware("http")
async def log_requests(request: Request, call_next):
    request_id = str(uuid.uuid4())
    start_time = time.time()
    
    # Capturer les données d'entrée pour les requêtes POST
    input_data = None
    if request.method == "POST" and request.url.path != "/predict":
        try:
            body = await request.body()
            if body:
                input_data = json.loads(body.decode())
        except:
            input_data = {"error": "Unable to parse request body"}
    
    # Traiter la requête
    response = await call_next(request)
    
    # Calculer le temps de réponse
    response_time_ms = round((time.time() - start_time) * 1000, 2)
    
    # Logger seulement si ce n'est pas une requête /predict (qui sera loggée séparément)
    if request.url.path != "/predict":
        try:
            log_request_to_daily_csv(
                request_id=request_id,
                endpoint=str(request.url.path),
                method=request.method,
                input_data=input_data,
                prediction_result=None,
                status_code=response.status_code,
                response_time_ms=response_time_ms,
                error_message=None if 200 <= response.status_code < 300 else f"HTTP {response.status_code}"
            )
            
            logger.info(f"Request {request_id}: {request.method} {request.url.path} - Status: {response.status_code} - Time: {response_time_ms}ms")
        except Exception as e:
            logger.error(f"Erreur lors du logging: {str(e)}")
    
    return response

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200"], 
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Définir la structure des données d'entrée
class CovidData(BaseModel):
    confirmed_case: int
    date: date
    deaths: int
    recovered: int
    location: str  # Format: "Country - region standard - location standard"
    region: str    # Format: "Country - region standard"
    country: str
    continent: str
    population: float
    who_region: str

# Mapping pour les continents et who_regions
continents_map = {
    'AFRICA': 0,
    'ASIA': 1,
    'EUROPE': 2,
    'NORTH_AMERICA': 3,
    'OCEANIA': 4,
    'SOUTH_AMERICA': 5
}

who_regions_map = {
    'Africa': 0,
    'Americas': 1,
    'Eastern_Mediterranean': 2,
    'Europe': 3,
    'South-East_Asia': 4,
    'Western_Pacific': 5
}

@app.get("/")
def read_root():
    return {"Covid-19 Prediction API": "API pour prédire le nombre de cas de Covid-19"}

@app.post("/predict")
async def predict(data: CovidData):
    request_id = str(uuid.uuid4())
    start_time = time.time()
    
    try:
        logger.info(f"Request {request_id}: Nouvelle requête de prédiction")
        
        # Features numériques (4 features)
        numeric_features = np.array([[
            data.confirmed_case,
            data.deaths,
            data.recovered,
            data.population
        ]])
        
        # One-hot encoding pour continent (6 features)
        continent_features = np.zeros((1, 6))
        if data.continent in continents_map:
            continent_features[0, continents_map[data.continent]] = 1
            
        # One-hot encoding pour who_region (6 features)
        who_region_features = np.zeros((1, 6))
        if data.who_region in who_regions_map:
            who_region_features[0, who_regions_map[data.who_region]] = 1
            
        # One-hot encoding pour location (250 features)
        location_features = np.zeros((1, 250))
        
        # One-hot encoding pour region (250 features)
        region_features = np.zeros((1, 250))
        
        # One-hot encoding pour country (211 features)
        country_features = np.zeros((1, 211))
        
        # Combiner tous les features
        all_features = np.hstack([
            numeric_features,           # 4
            continent_features,         # 6
            who_region_features,        # 6
            location_features,          # 250
            region_features,           # 250
            country_features           # 211
        ])
        
        logger.debug(f"Nombre total de features: {all_features.shape[1]}")
        
        # Faire la prédiction
        prediction = model.predict(all_features)[0]
        probabilities = model.predict_proba(all_features)[0]
        probability = float(max(probabilities))
        
        response_time_ms = round((time.time() - start_time) * 1000, 2)
        
        result = {
            "request_id": request_id,
            "prediction": int(prediction),
            "probability": probability,
            "features_length": all_features.shape[1],
            "response_time_ms": response_time_ms
        }
        
        logger.info(f"Request {request_id}: Prédiction réussie - {prediction} (probabilité: {probability:.3f}) en {response_time_ms}ms")
        
        # Logger les détails de la prédiction dans le CSV quotidien
        try:
            log_request_to_daily_csv(
                request_id=request_id,
                endpoint="/predict",
                method="POST",
                input_data=data.dict(),
                prediction_result=result,
                status_code=200,
                response_time_ms=response_time_ms
            )
            logger.info(f"Request {request_id}: Données loggées avec succès dans le CSV")
        except Exception as log_error:
            logger.error(f"Request {request_id}: Erreur lors du logging CSV - {str(log_error)}")
        
        return result
        
    except Exception as e:
        response_time_ms = round((time.time() - start_time) * 1000, 2)
        error_msg = str(e)
        
        logger.error(f"Request {request_id}: Erreur lors de la prédiction - {error_msg} en {response_time_ms}ms")
        
        # Logger l'erreur dans le CSV quotidien
        try:
            log_request_to_daily_csv(
                request_id=request_id,
                endpoint="/predict",
                method="POST",
                input_data=data.dict() if 'data' in locals() else None,
                prediction_result={"error": error_msg},
                status_code=400,
                response_time_ms=response_time_ms,
                error_message=error_msg
            )
        except Exception as log_error:
            logger.error(f"Request {request_id}: Erreur lors du logging CSV d'erreur - {str(log_error)}")
        
        raise HTTPException(status_code=400, detail=error_msg)
