from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from datetime import date
import joblib
import numpy as np
import pandas as pd
import logging
import os

# Configuration des logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

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
    try:
        logger.info("Réception d'une nouvelle requête de prédiction")
        
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
        
        logger.info(f"Prédiction effectuée avec succès: {prediction}")
        
        return {
            "prediction": int(prediction),
            "probability": probability,
            "features_length": all_features.shape[1]
        }
    except Exception as e:
        logger.error(f"Erreur lors de la prédiction: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))
