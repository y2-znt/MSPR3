# 📋 Guide du Changelog Automatique

Ce document explique le fonctionnement du système de changelog automatique mis en place pour le projet MSPR3.

## 🎯 Vue d'ensemble

Le changelog est automatiquement généré à partir des messages de commit suivant la convention [Conventional Commits](https://www.conventionalcommits.org/fr/v1.0.0/). Chaque commit bien formaté contribue automatiquement au changelog du projet.

## 📝 Convention de commit

### Format de base
```
<type>[portée optionnelle]: <description>

[corps optionnel]

[pied de page optionnel]
```

### Types de commits supportés

| Type | Emoji | Description | Apparition dans changelog |
|------|-------|-------------|---------------------------|
| `feat` | 🚀 | Nouvelle fonctionnalité | ✅ Nouvelles fonctionnalités |
| `fix` | 🐛 | Correction de bug | ✅ Corrections de bugs |
| `perf` | ⚡ | Amélioration de performance | ✅ Améliorations de performance |
| `refactor` | ♻️ | Refactoring du code | ✅ Refactoring |
| `docs` | 📚 | Documentation | ✅ Documentation |
| `style` | 💄 | Style et formatage | ✅ Style et formatage |
| `test` | ✅ | Tests | ✅ Tests |
| `build` | 🔧 | Build et outils | ✅ Build et CI/CD |
| `ci` | 👷 | Intégration continue | ✅ Intégration continue |
| `chore` | 🔨 | Maintenance | ✅ Maintenance |
| `revert` | ⏪ | Annulation | ✅ Annulations |

### Types spécifiques au projet MSPR3

| Type | Emoji | Description |
|------|-------|-------------|
| `etl` | 🔄 | Processus ETL |
| `ai` | 🤖 | Intelligence Artificielle |
| `data` | 📊 | Gestion des données |
| `api` | 🌐 | API |
| `ui` | 🎨 | Interface utilisateur |
| `security` | 🔒 | Sécurité |
| `config` | ⚙️ | Configuration |

### Portées (scopes) recommandées

| Portée | Description | Emoji |
|--------|-------------|-------|
| `backend` | Backend Spring Boot | 🏗️ |
| `frontend` | Frontend Angular | 🎨 |
| `ai-api` | API Intelligence Artificielle | 🤖 |
| `ai-training` | Entraînement IA | 🧠 |
| `etl` | Processus ETL | 🔄 |
| `database` | Base de données | 🗄️ |
| `docker` | Configuration Docker | 🐳 |
| `ci` | CI/CD | 👷 |
| `docs` | Documentation | 📚 |
| `config` | Configuration | ⚙️ |
| `security` | Sécurité | 🔒 |
| `performance` | Performance | ⚡ |

## 🔄 Fonctionnement automatique

### Déclenchement
Le changelog est généré automatiquement :
- À chaque push sur la branche `main`
- Manuellement via le workflow GitHub Actions
- Lors de la création d'une release

### Processus
1. **Analyse des commits** : Le système analyse tous les nouveaux commits depuis la dernière génération
2. **Catégorisation** : Les commits sont classés par type et portée
3. **Génération** : Le fichier `CHANGELOG.md` est mis à jour
4. **Commit automatique** : Les changements sont committés avec le message `docs: mise à jour automatique du changelog [skip-changelog]`

### Éviter les boucles infinies
Les commits contenant `[skip-changelog]` dans le message ne déclenchent pas la génération du changelog.

## 📋 Structure du changelog

Le changelog généré suit cette structure :

```markdown
# 📋 Changelog - Plateforme OMS de Suivi des Pandémies

## [Version] - Date

### 🚀 Nouvelles fonctionnalités
- feat(frontend): ajout du dashboard de visualisation des données
- feat(ai): implémentation du modèle de prédiction

### 🐛 Corrections de bugs
- fix(backend): correction de la validation des données ETL
- fix(api): résolution du problème d'authentification

### 📚 Documentation
- docs: mise à jour du guide d'installation
- docs(api): ajout de la documentation Swagger
```

## 🛠️ Utilisation manuelle

### Générer le changelog localement
```bash
# Installation des dépendances
npm install

# Génération du changelog depuis le dernier tag
npm run changelog

# Génération complète du changelog
npm run changelog:init
```

### Créer une release
```bash
# Release automatique (patch)
npm run release

# Release majeure
npm run release:major

# Release mineure  
npm run release:minor

# Release patch
npm run release:patch
```

## 🎯 Bonnes pratiques

### ✅ Exemples de bons commits
```bash
# Nouvelle fonctionnalité
git commit -m "feat(backend): ajout de l'API de gestion des alertes pandémiques"

# Correction de bug avec référence d'issue
git commit -m "fix(frontend): correction de l'affichage des graphiques (#123)"

# Amélioration de performance
git commit -m "perf(etl): optimisation du traitement des données CSV"

# Breaking change
git commit -m "feat(api)!: refonte de l'authentification OAuth2

BREAKING CHANGE: l'ancien système d'authentification par token est supprimé"
```

### ❌ Exemples à éviter
```bash
# Trop vague
git commit -m "fix bug"

# Pas de type
git commit -m "correction dashboard"

# Type incorrect
git commit -m "add: nouvelle page"
```

## 🔧 Configuration avancée

### Personnaliser les catégories
Modifiez le fichier `.github/changelog-config.js` pour :
- Ajouter de nouveaux types de commits
- Modifier les emojis et libellés
- Personnaliser le formatage

### Désactiver temporairement
Ajoutez `[skip-changelog]` dans votre message de commit :
```bash
git commit -m "chore: nettoyage du code [skip-changelog]"
```

## 🐛 Dépannage

### Le changelog n'est pas généré
1. Vérifiez que le commit suit la convention
2. Assurez-vous que le workflow GitHub Actions est activé
3. Vérifiez les permissions du token GitHub

### Erreur de format
1. Validez vos messages de commit avec [conventional-commits](https://www.conventionalcommits.org/)
2. Utilisez des outils comme `commitizen` pour vous aider

### Changelog vide
1. Assurez-vous d'avoir des commits avec les types supportés
2. Vérifiez la configuration dans `.github/changelog-config.js`

## 📚 Ressources

- [Conventional Commits](https://www.conventionalcommits.org/fr/)
- [Keep a Changelog](https://keepachangelog.com/fr/)
- [Semantic Versioning](https://semver.org/lang/fr/)
- [conventional-changelog](https://github.com/conventional-changelog/conventional-changelog)

---

Pour toute question ou suggestion d'amélioration, ouvrez une issue sur le [repository GitHub](https://github.com/y2-znt/MSPR3/issues).
