# Gestion de Projet

## Méthodologie adoptée

Pour mener à bien ce projet, nous avons mis en place une organisation basée sur les principes de l’agilité. Sans suivre une méthode stricte comme Scrum, nous nous sommes inspirés de ses pratiques (réunions courtes quotidiennes, rétrospectives hebdomadaires, répartition des tâches en fonction des priorités) pour structurer notre travail de manière efficace. L’objectif était de rester flexibles tout en gardant un rythme de développement régulier.

## Outils utilisés

### Suivi et répartition des tâches

Nous avons utilisé Trello pour organiser les tâches de manière claire. Le tableau principal était divisé en colonnes représentant l’avancement (à faire, en cours, terminé), avec des cartes classées par domaine (Frontend, Backend, DevOps, Documentation, etc.). Cela nous a permis de mieux répartir le travail entre nous et de suivre l’avancement global du projet.

### Communication d’équipe

La coordination s’est faite principalement sur Discord. Ce serveur a centralisé toutes les discussions, les notifications Gitlab, Github, les mises à jour Trello, et a facilité la prise de décision rapide. Ce choix nous a permis de gagner du temps et de rester synchronisés, même en travaillant à distance.

### Versioning et intégration continue

Nous avons commencé le projet sur GitLab, mais les limites de pipelines gratuites nous ont rapidement freinés. Nous avons donc migré vers Github, ce qui nous a permis d’exécuter plus librement nos workflows CI/CD. Cette décision a eu un impact positif sur le développement, notamment pour les tests automatisés et les builds.

## Organisation du travail

### Réunions

Chaque jour, une courte réunion nous permettait de faire un point rapide sur l’avancement de chacun et de lever les éventuels blocages. Une fois par semaine, une réunion plus longue servait à revoir les objectifs de la semaine et à ajuster la répartition du travail si nécessaire.

### Revue de code

Les revues de code étaient planifiées régulièrement. Nous les avons utilisées non seulement pour vérifier la qualité du code, mais aussi pour échanger sur nos choix techniques et progresser ensemble. Cela a renforcé l’homogénéité de notre base de code et permis de garder un bon niveau de qualité.

## Déroulé du projet

Le projet s’est déroulé en plusieurs phases successives :

- **Phase de conception** : définition des besoins et des fonctionnalités, création de maquettes, identification des données nécessaires, etc.
- **Phase DevOps** : mise en place de l’environnement technique, Dockerisation, automatisation des déploiements et configuration des pipelines.
- **Phase Backend** : développement de l’API avec Spring Boot, en veillant à la clarté de la structure et à la documentation grâce à Swagger.
- **Phase Data** : préparation et nettoyage des données via un processus ETL sur mesure, garantissant leur qualité dès le départ.
- **Phase Frontend** : création de l’interface utilisateur avec Angular, en assurant une bonne intégration avec l’API et une expérience fluide.
- **Phase Documentation** : rédaction de la documentation technique et utilisateur, accompagnée de schémas et de guides clairs.

## Suivi de la qualité

Nous avons défini quelques indicateurs simples pour suivre notre progression : tâches Trello complétées, taux de couverture des tests, délais de résolution des bugs. Ces données nous ont aidés à identifier les points à améliorer et à ajuster notre rythme au fil des semaines.

## Bilan et retours d’expérience

Ce projet nous a permis de mettre en pratique une gestion de projet complète, de la planification initiale au produit final. La migration vers GitHub, les outils de communication choisis et l’organisation agile ont été des éléments clés dans notre réussite. Travailler en équipe sur toutes les dimensions du projet nous a aussi permis de mieux comprendre l’importance de la coordination et de la rigueur dans le développement d’une application.
