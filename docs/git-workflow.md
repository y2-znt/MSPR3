# Documentation : Organisation et Conventions Git

## Structure des branches

Nous adoptons un workflow Git simple et efficace, centré autour d’une branche principale :

- `main` : la branche de référence contenant le code en production.
- Branches de fonctionnalité : créées depuis `main` pour chaque nouvelle tâche ou correction.

Cette approche garantit un historique propre et une gestion simplifiée des versions.

---

## Explication des Branches

### `main`

- **Rôle :**
  - Branche principale, représentant le code actuellement en production.
  - Toujours stable, aucun développement direct n'y est réalisé.
- **Utilisation :**
  - Les modifications proviennent exclusivement des branches de fonctionnalité via des Pull Requests.
  - Toutes les livraisons (releases) sont générées à partir de cette branche.

### Branches de fonctionnalité (`feature branches`)

- **Rôle :** Développement des nouvelles fonctionnalités ou corrections.
- **Convention de nommage :** `feature/nom-fonctionnalité`
  - Exemple : `feature/authentification-google`
- **Point de départ :** `main`
- **Point de fin :** Merge dans `main` après validation.

---

## Conventions de Nommage

### Branches

- **Syntaxe générale :** `{type}/{description}`
  - Types recommandés : `feature`, `fix`, `chore`, `refactor`, etc.
- **Exemples :**
  - `feature/authentification-google`
  - `fix/correction-timeout-api`

### Commits

- Format recommandé :

```php-template
<type> : <description>
```

- Types courants :

  - `feat` : nouvelle fonctionnalité
  - `fix` : correction de bug
  - `docs` : documentation
  - `style` : mise en forme, indentation
  - `refactor` : restructuration sans changement fonctionnel
  - `test` : ajout ou modification de tests
  - `chore` : maintenance, tâches annexes
  - `release` : publication d’une version

- Exemples :
  - `feat: ajout de la connexion avec Google`
  - `fix: correction du timeout sur les requêtes`
  - `release: v1.0.2`

---

## Workflow de développement

1. **Création d'une nouvelle branche**

   ```bash
   git checkout main
   git pull
   git checkout -b auth-system
   ```

2. **Commits clairs et cohérents**

   Respectez le format :

   ```
   <type> : <description>
   ```

   Exemples :

   ```
   feat : ajout de l'authentification Google
   fix: correction du timeout des requêtes API
   docs : mise à jour du README
   ```

3. **Mise à jour de la branche avant un merge**
   ```bash
   git checkout auth-system
   git pull origin main
   ```

---

## Bonnes pratiques

### Gestion des commits

- **Atomicité** : Un commit = une fonctionnalité/correction
- **Messages descriptifs** : Suivre les conventions de commit
- **Fréquence** : Commiter régulièrement pour éviter les gros changements
- **Qualité** : Tester les modifications avant chaque commit

### Gestion des branches

- **Synchronisation** : Maintenir sa branche à jour avec `main`
- **Protection** : Ne jamais pusher directement sur `main`
- **Nettoyage** : Supprimer les branches mergées
- **Nommage** : Utiliser des noms explicites pour les branches

### Rédaction de Pull Requests

- **Titre clair :** Résumez le travail effectué.
- **Description détaillée :** Ajoutez le contexte, les modifications apportées, et le lien vers une issue (si applicable).
- **Checklist avant merge :**
  - Tests unitaires passés.
  - Code review approuvée.
  - Conformité avec les conventions de codage.

### Revue de Code

- Toujours effectuer une revue par un pair avant tout merge.
- Vérifier la clarté, l'optimisation et la conformité des modifications proposées.
