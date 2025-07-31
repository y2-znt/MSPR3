#!/usr/bin/env python3
"""
Script simple pour analyser les logs quotidiens de l'API
"""

import pandas as pd
import os
import glob
from datetime import datetime, timedelta

def analyze_daily_logs(date_str=None):
    """Analyse les logs d'une date spécifique ou d'aujourd'hui"""
    if date_str is None:
        date_str = datetime.now().strftime('%Y-%m-%d')
    
    log_file = f"api_requests_{date_str}.csv"
    
    if not os.path.exists(log_file):
        print(f"Fichier de log non trouvé: {log_file}")
        return
    
    try:
        df = pd.read_csv(log_file)
        print(f"=== ANALYSE DES LOGS DU {date_str} ===")
        print(f"Nombre total de requêtes: {len(df)}")
        
        if len(df) == 0:
            print("Aucune requête trouvée")
            return
        
        # Analyse par endpoint
        print(f"\nRépartition par endpoint:")
        endpoint_counts = df['endpoint'].value_counts()
        for endpoint, count in endpoint_counts.items():
            print(f"  {endpoint}: {count} requêtes")
        
        # Analyse des codes de statut
        print(f"\nCodes de statut:")
        status_counts = df['status_code'].value_counts()
        for status, count in status_counts.items():
            percentage = (count / len(df)) * 100
            print(f"  {status}: {count} requêtes ({percentage:.1f}%)")
        
        # Analyse des temps de réponse
        response_times = df['response_time_ms'].dropna()
        if len(response_times) > 0:
            print(f"\nTemps de réponse (ms):")
            print(f"  Moyenne: {response_times.mean():.2f}")
            print(f"  Médiane: {response_times.median():.2f}")
            print(f"  Min: {response_times.min():.2f}")
            print(f"  Max: {response_times.max():.2f}")
        
        # Analyse des prédictions
        predictions_df = df[(df['endpoint'] == '/predict') & (df['status_code'] == 200)]
        if len(predictions_df) > 0:
            print(f"\nPrédictions réussies: {len(predictions_df)}")
            
            # Distribution des prédictions
            predictions = predictions_df['prediction'].dropna()
            if len(predictions) > 0:
                print(f"Distribution des prédictions:")
                pred_counts = predictions.value_counts().sort_index()
                for pred, count in pred_counts.items():
                    print(f"  Prédiction {pred}: {count} fois")
            
            # Statistiques des probabilités
            probabilities = predictions_df['probability'].dropna()
            if len(probabilities) > 0:
                print(f"\nProbabilités:")
                print(f"  Moyenne: {probabilities.mean():.3f}")
                print(f"  Min: {probabilities.min():.3f}")
                print(f"  Max: {probabilities.max():.3f}")
        
        # Erreurs
        errors_df = df[df['status_code'] >= 400]
        if len(errors_df) > 0:
            print(f"\nErreurs ({len(errors_df)} total):")
            error_messages = errors_df['error_message'].value_counts()
            for error, count in error_messages.head(5).items():
                print(f"  {error}: {count} fois")
        
    except Exception as e:
        print(f"Erreur lors de l'analyse: {e}")

def list_available_logs():
    """Liste tous les fichiers de logs disponibles"""
    log_files = glob.glob("api_requests_*.csv")
    if not log_files:
        print("Aucun fichier de log trouvé")
        return []
    
    print("Fichiers de logs disponibles:")
    dates = []
    for log_file in sorted(log_files):
        date_str = log_file.replace('api_requests_', '').replace('.csv', '')
        dates.append(date_str)
        
        # Compter les requêtes dans ce fichier
        try:
            df = pd.read_csv(log_file)
            count = len(df)
            print(f"  {date_str}: {count} requêtes")
        except:
            print(f"  {date_str}: erreur de lecture")
    
    return dates

def analyze_last_n_days(n_days=7):
    """Analyse les logs des n derniers jours"""
    print(f"=== ANALYSE DES {n_days} DERNIERS JOURS ===")
    
    total_requests = 0
    total_predictions = 0
    total_errors = 0
    
    for i in range(n_days):
        date = datetime.now() - timedelta(days=i)
        date_str = date.strftime('%Y-%m-%d')
        log_file = f"api_requests_{date_str}.csv"
        
        if os.path.exists(log_file):
            try:
                df = pd.read_csv(log_file)
                requests = len(df)
                predictions = len(df[(df['endpoint'] == '/predict') & (df['status_code'] == 200)])
                errors = len(df[df['status_code'] >= 400])
                
                total_requests += requests
                total_predictions += predictions
                total_errors += errors
                
                if requests > 0:
                    print(f"  {date_str}: {requests} requêtes, {predictions} prédictions, {errors} erreurs")
            except:
                pass
    
    print(f"\nTOTAL ({n_days} jours):")
    print(f"  Requêtes: {total_requests}")
    print(f"  Prédictions réussies: {total_predictions}")
    print(f"  Erreurs: {total_errors}")
    if total_requests > 0:
        error_rate = (total_errors / total_requests) * 100
        print(f"  Taux d'erreur: {error_rate:.1f}%")

def main():
    """Fonction principale"""
    print("ANALYSEUR DE LOGS API")
    print("=" * 40)
    
    # Analyser aujourd'hui
    print("1. Analyse d'aujourd'hui:")
    analyze_daily_logs()
    
    print("\n" + "=" * 40)
    
    # Analyser les 7 derniers jours
    print("2. Résumé des 7 derniers jours:")
    analyze_last_n_days(7)
    
    print("\n" + "=" * 40)
    
    # Lister tous les fichiers disponibles
    print("3. Fichiers de logs disponibles:")
    list_available_logs()

if __name__ == "__main__":
    main() 