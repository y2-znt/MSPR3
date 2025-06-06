# Documentation de la Configuration Nginx

Ce document explique en détail la configuration Nginx utilisée dans notre application. Le fichier `nginx.conf` est configuré pour servir notre application Angular et faire office de reverse proxy pour nos APIs.

## Vue d'ensemble

Notre configuration Nginx :

- Redirige tout le trafic HTTP vers HTTPS
- Gère les certificats SSL
- Fait office de reverse proxy pour le backend et l'API AI
- Sert l'application Angular
- Optimise la performance avec la compression gzip
- Gère le cache des assets statiques

## Configuration détaillée

### 1. Redirection HTTP vers HTTPS

```nginx
server {
    listen 80;
    return 301 https://$host$request_uri;
}
```

Cette section :

- Écoute sur le port 80 (HTTP)
- Redirige automatiquement vers HTTPS avec un code 301 (redirection permanente)
- `$host` : nom de domaine de la requête
- `$request_uri` : chemin et paramètres de la requête

### 2. Configuration HTTPS

```nginx
server {
    listen 443 ssl http2;
    server_name $SERVER_NAME;

    ssl_certificate /etc/nginx/certs/nginx-selfsigned.crt;
    ssl_certificate_key /etc/nginx/certs/nginx-selfsigned.key;
}
```

Cette section :

- Écoute sur le port 443 (HTTPS)
- Active le support HTTP/2
- Utilise les certificats SSL auto-signés
- `$SERVER_NAME` est remplacé par l'IP de la VM ou le nom de domaine

### 3. Configuration du Reverse Proxy Backend

```nginx
location /api/ {
    proxy_pass http://backend:8080/api/;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_cache_bypass $http_upgrade;
}
```

Cette section :

- Route les requêtes `/api/*` vers le service backend
- Configure les en-têtes pour le proxy :
  - `Host` : préserve le nom d'hôte original
  - `X-Real-IP` : IP du client
  - `X-Forwarded-For` : chaîne d'IPs proxy
  - `X-Forwarded-Proto` : protocole utilisé (http/https)
- Support des WebSockets avec `Upgrade` et `Connection`

### 4. Configuration du Reverse Proxy AI API

```nginx
location = /predict {
    proxy_pass http://ai-api:80/predict;
    # ... mêmes headers que pour le backend ...
}
```

Cette section :

- Route exactement `/predict` vers l'API AI
- Utilise les mêmes configurations de proxy que le backend

### 5. Gestion des Assets Statiques

```nginx
location ~* \.(?:ico|css|js|gif|jpe?g|png|woff2?|eot|ttf|otf|svg)$ {
    expires 1y;
    access_log off;
    add_header Cache-Control "public";
}
```

Cette section :

- Gère tous les fichiers statiques (images, styles, fonts...)
- Configure le cache navigateur à 1 an
- Désactive les logs pour ces fichiers
- Ajoute l'en-tête pour le cache public

### 6. Configuration Angular (HTML5 Mode)

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

Cette section :

- Supporte le routing Angular en HTML5 mode
- Cherche d'abord le fichier exact
- Puis cherche le dossier
- Sinon renvoie index.html (pour le routing côté client)

### 7. Compression Gzip

```nginx
gzip on;
gzip_vary on;
gzip_proxied any;
gzip_comp_level 6;
gzip_buffers 16 8k;
gzip_http_version 1.1;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
```

Cette section :

- Active la compression gzip
- Configure le niveau de compression (6)
- Spécifie les types MIME à compresser
- Optimise les buffers pour la compression

## Bonnes Pratiques de Sécurité

1. **Redirection HTTPS**

   - Tout le trafic HTTP est redirigé vers HTTPS
   - Protection contre le sniffing

2. **Headers de Sécurité**

   - `X-Real-IP` et `X-Forwarded-For` pour le tracking
   - Support des WebSockets sécurisé

3. **Cache Optimisé**
   - Cache long pour les assets statiques
   - Pas de cache pour les API
   - Headers de cache appropriés

## Troubleshooting

### Problèmes Courants

1. **Erreur 502 Bad Gateway**

   - Vérifier que les services backend/ai-api sont up
   - Vérifier les logs : `docker-compose logs nginx`

2. **Certificat SSL non reconnu**

   - Normal avec un certificat auto-signé
   - Ajouter une exception dans le navigateur
   - En production, utiliser Let's Encrypt

3. **Problèmes de routing Angular**
   - Vérifier la configuration `try_files`
   - Vérifier les logs Angular dans la console navigateur

### Commandes Utiles

```bash
# Test de la configuration
docker-compose exec frontend nginx -t

# Reload de la configuration
docker-compose exec frontend nginx -s reload

# Voir les logs en temps réel
docker-compose logs -f frontend
```

## Pour la Production

Pour un environnement de production, considérer :

1. Remplacer les certificats auto-signés par Let's Encrypt
2. Ajouter des headers de sécurité supplémentaires :
   ```nginx
   add_header Strict-Transport-Security "max-age=31536000" always;
   add_header X-Frame-Options "SAMEORIGIN" always;
   add_header X-XSS-Protection "1; mode=block" always;
   add_header X-Content-Type-Options "nosniff" always;
   ```
3. Configurer des limites de rate :
   ```nginx
   limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
   location /api/ {
       limit_req zone=api_limit burst=20 nodelay;
       # ... reste de la config ...
   }
   ```
