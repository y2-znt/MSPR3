# Documentation : Organisation et Conventions Git

## Structure des branches

Notre workflow Git est basé sur une approche simple et efficace, centrée sur la branche principale :

- `main` : Branche principale contenant le code de production
- Branches de fonctionnalités : Créées directement depuis `main` pour chaque nouvelle fonctionnalité ou correction

## Workflow de développement

1. **Création d'une nouvelle branche**

   ```bash
   git checkout main
   git pull
   git checkout -b auth-system
   ```

2. **Conventions de commit**

   Les messages de commit doivent suivre strictement le format :

   ```
   <type> : <description>
   ```

   Types de commit :

   - `feat` : nouvelle fonctionnalité
   - `fix` : correction de bug
   - `docs` : modification de la documentation
   - `style` : formatage, point-virgules manquants, etc.
   - `refactor` : refactorisation du code
   - `test` : ajout ou modification de tests
   - `chore` : mise à jour des outils, scripts, etc.

   Exemples :

   ```
   feat : ajout de l'authentification Google
   fix: correction du timeout des requêtes API
   docs : mise à jour du README
   ```

3. **Mise à jour de la branche**
   ```bash
   git checkout auth-system
   git pull origin main
   ```

## Bonnes pratiques

### Gestion des commits

- **Atomicité** : Un commit = une fonctionnalité/correction
- **Messages descriptifs** : Suivre les conventions de commit
- **Fréquence** : Commiter régulièrement pour éviter les gros changements
- **Vérification** : Tester les modifications avant chaque commit

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
