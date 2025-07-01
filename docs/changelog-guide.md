# 📋 Documentation du Changelog Automatique

## 🎯 Vue d'ensemble

Ce système génère automatiquement un changelog à partir des commits de la branche `main`, en supprimant les doublons et en organisant les changements par catégories.

## � Fonctionnement

### Déclenchement
- **Manuel** : Via `workflow_dispatch` dans GitHub Actions
- **Automatique** : Push sur la branche `changelog-ci`
- **Orchestré** : Déclenché par le job `trigger-changelog` après les déploiements

### Processus
1. **Checkout** sur la branche `changelog-ci`
2. **Récupération** des 50 derniers commits de `main`
3. **Déduplication** basée sur le message du commit (après le `:`)
4. **Limitation** à 30 commits maximum
5. **Catégorisation** automatique
6. **Génération** du fichier `CHANGELOG.md`
7. **Commit et push** automatique

## � Structure du Changelog

```markdown
# Changelog

All notable changes to this project will be documented in this file.

## 2025-07-01

### Features
* feat: nouvelle fonctionnalité ([commit](link))

### Bug Fixes
* fix: correction d'un bug ([commit](link))

### Other Changes
* refactor: amélioration du code ([commit](link))
```

## 🏷️ Catégories automatiques

| Préfixe | Catégorie | Description |
|---------|-----------|-------------|
| `feat:`, `add:`, `implement:`, `create:` | **Features** | Nouvelles fonctionnalités |
| `fix:`, `correct:`, `update:`, `remove:` | **Bug Fixes** | Corrections et mises à jour |
| Autres | **Other Changes** | Refactoring, docs, etc. |
|--------|-------------|-------|
## ⚙️ Configuration

### Fichiers clés
- `.github/workflows/changelog.yml` - Workflow principal
- `.github/changelog-config.js` - Configuration conventional-changelog

### Paramètres
- **Source** : Commits de `origin/main`
- **Limite** : 30 commits maximum
- **Format** : Conventional Commits (Angular preset)
- **Destination** : Branche `changelog-ci`

## 🔧 Personnalisation

### Modifier les catégories
```bash
# Dans le script AWK, modifier les patterns :
grep "^\* feat\|^\* add\|^\* NOUVEAU_PREFIX" commits.tmp
```

### Changer la limite de commits
```bash
# Modifier dans le script :
head -50  # ← Commits récupérés
count < 30  # ← Limite finale
```

### Ajuster les préfixes
```yaml
# Ajouter de nouveaux préfixes dans les grep :
grep "^\* feat\|^\* add\|^\* VOTRE_PREFIX"
```

## 🛠️ Dépannage

### Changelog vide
- Vérifier que `origin/main` existe
- Contrôler les patterns de filtrage
- Examiner les logs de debug

### Doublons persistants
- Le script compare les messages **après** le `:`
- Vérifier la logique de déduplication dans AWK

### Erreurs de format
- S'assurer que les commits suivent le format `type: description`
- Vérifier les expressions régulières

## 📋 Exemple d'utilisation

### Déclenchement manuel
```bash
# Via GitHub UI : Actions → Generate Changelog → Run workflow
```

### Intégration CI/CD
Le changelog se génère automatiquement après chaque déploiement réussi sur `main` grâce au job `trigger-changelog`.

---

**💡 Tip** : Pour exclure un commit du changelog, utilisez `[skip-changelog]` dans le message de commit.
