---

# CI Documentation – Génération automatique avec Pandoc

Cette documentation explique comment mettre en place une **Intégration Continue (CI)** avec **Pandoc** via **GitHub Actions**, afin de convertir automatiquement les fichiers Markdown (`.md`) de votre dossier `docs/` en **PDF** et **HTML**, puis de les publier via **GitHub Pages**.

---

##  Prérequis

* Un dépôt GitHub structuré avec :

  * Des fichiers Markdown dans `docs/`
  * Un en-tête LaTeX (optionnel) dans `docs/latex/header.tex`
  * Une feuille de style CSS (optionnelle) dans `docs/latex/style.css`
  * GitHub Pages activé sur la branche `gh-pages`

---

##  Fonctionnement du workflow CI

Le fichier `.github/workflows/docs.yml` contient l'ensemble des étapes suivantes :

### 🔁 Déclencheurs

```yaml
on:
  push:
    paths:
      - 'docs/**'
      - '.github/workflows/docs.yml'
```

Le workflow est déclenché à chaque *push* sur des fichiers du dossier `docs/` ou sur le fichier de workflow lui-même. 

---

## Étapes du workflow

### 1. **Installation des outils**

```yaml
- name: Install Pandoc and LaTeX
  run: |
    sudo apt-get update
    sudo apt-get install -y pandoc texlive-latex-base texlive-latex-recommended texlive-latex-extra texlive-xetex imagemagick
```

>  Cette étape installe Pandoc, LaTeX (XeLaTeX) et ImageMagick pour générer les documents PDF.

---

### 2. **Conversion des fichiers `.md` en PDF**

Tous les fichiers Markdown de `docs/` sont convertis individuellement en PDF et placés dans `docs/pdfs/`.

```bash
find docs/ -name '*.md' | while read mdfile; do
  # conversion PDF avec Pandoc
done
```

---

### 3. **Conversion des images PNG/JPG en PDF**

Les images présentes dans `docs/` et `docs/ressources/` sont converties en PDF dans `pdf/appendices/` via ImageMagick. 

---

### 4. **Fusion des fichiers Markdown**

Les fichiers Markdown sont concaténés en un seul fichier `combined.md` selon un ordre logique :

1. `brief_part1.md`
2. Fichiers du dossier `benchmark/`
3. Fichiers du dossier `architecture/`
4. `tech-stack.md`
5. Tous les autres fichiers `.md` restants
6. Annexe en fin de document

---

### 5. **Génération du PDF principal**

```yaml
- name: Generate Master PDF
  run: |
    pandoc combined.md \
      --resource-path=... \
      -s \
      --pdf-engine=xelatex \
      --toc \
      --toc-depth=2 \
      --include-in-header=docs/latex/header.tex \
      -o pdf/documentation.pdf
```

> Ce fichier PDF complet est stocké dans `pdf/documentation.pdf`.

---
 
### 6. **Génération de la documentation HTML**

```yaml
- name: Generate HTML documentation
  run: |
    pandoc combined.md \
      -c docs/latex/style.css \
      -M title="Documentation Technique" \
      -o html/index.html
```

> La page HTML est stylisée avec `style.css` si présent, puis déployée sur GitHub Pages.

---

### 7. **Déploiement sur GitHub Pages**

```yaml
- name: Deploy to GitHub Pages
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./html
```

> Le contenu HTML est publié automatiquement sur la branche `gh-pages`.

---

### 8. **Artifacts**

Deux artefacts sont créés pour permettre le téléchargement depuis l'interface GitHub :

* `All PDFs` : tous les PDFs individuels générés
* `Master PDF` : le document global

---

##  Résultat attendu

*  Des fichiers PDF et HTML sont générés automatiquement à chaque push.
*  La documentation HTML est disponible via GitHub Pages.
*  Les fichiers PDF sont téléchargeables depuis l’interface des Actions GitHub (Artifacts).

---

##  Bonnes pratiques

* Organisez vos fichiers Markdown par dossier logique (`benchmark/`, `architecture/`, etc.)
* Gardez un style cohérent dans vos titres et niveaux de titres (`#`, `##`, etc.)
* Vérifiez la compatibilité des images utilisées dans les documents.

---
