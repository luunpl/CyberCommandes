========================================================================
VRP OPTIMIZER - PLANIFICATEUR DE TOURNEES
Projet Intégrateur - L3 MIAGE (Groupe 8)
========================================================================

MEMBRES DE L'EQUIPE :
--------------------
- MOUSSA MOHAMED Kalid
- DIALLO Mamadou
- FOFANA Moussa Donatien
- LUU Nguyen Phuoc Loc
- THERON Maxence
- WHANNOU Irving

------------------------------------------------------------------------
1. PRESENTATION DU PROJET
------------------------------------------------------------------------
VRP Optimizer est une application web métier conçue pour résoudre le
Problème de Tournées de Véhicules (VRP - Vehicle Routing Problem).
Elle permet aux planificateurs logistiques de charger des commandes
depuis une base de données, de visualiser les points de livraison sur
une carte interactive, et de générer des tournées optimisées pour une
flotte de véhicules afin de minimiser les coûts (kilométrage) et les
temps de trajet.

L'application est optimisée pour la gestion de gros volumes de données
grâce à un système de cache intelligent et des algorithmes hybrides.

------------------------------------------------------------------------
2. FONCTIONNALITES PRINCIPALES
------------------------------------------------------------------------
* Cartographie Interactive : Visualisation via Leaflet.
* Algorithmes Multiples : 4 stratégies d'optimisation adaptées aux
  besoins métiers (Sectorisation, Kilométrage, Groupement).
* Synchronisation BDD : Importation des commandes et sauvegarde des
  tournées calculées.
* Cache de Distances : Système unique de stockage des matrices de
  trajets en base de données pour éviter les appels API redondants.
* Outil d'Aide à la Décision : Comparatif temps/distance/véhicules.
* Routage Résilient : Fallback automatique en ligne droite en cas de
  blocage API (Rate Limiting) pour garantir l'affichage des routes.

------------------------------------------------------------------------
3. STACK TECHNIQUE & ARCHITECTURE
------------------------------------------------------------------------
Frontend (Interface Client) :
- Framework : Angular (Signals, Standalone Components).
- Cartographie : Leaflet & OpenRouteService API.
- Génération d'ID : Système compatible HTTP (Time-based IDs).

Backend (Serveur & Logique Métier) :
- Framework : Spring Boot 3 (Java 21).
- Base de données : PostgreSQL (Production) / H2 (Tests).
- Cache Local : Stockage des distances/temps ORS en base de données
  (`distance_cache_entity`) pour une exécution instantanée.

Qualité & Robustesse :
- Tests d'intégration (IT) : Couverture complète des contrôleurs via
  WebTestClient.
- Rapport de Coverage : Analyse de couverture via JaCoCo/IntelliJ
  (visée > 80% sur les contrôleurs ).

------------------------------------------------------------------------
4. LES 4 ALGORITHMES D'OPTIMISATION
------------------------------------------------------------------------
L'application implémente des logiques de type "Cluster-First, Route-Second" :

1. Grid Strict (Radar/Sweep) :
  - Principe : Tri angulaire autour du dépôt et découpage en secteurs.
  - Usage : Création de zones de livraison claires et sans entrecroisement.

2. Greedy Strict (Glouton local) :
  - Principe : Recherche du point le plus proche à chaque étape via
    la matrice de distances.
  - Usage : Optimisation rapide des distances sur des zones denses.

3. Clarke & Wright (Méthode des Economies) :
  - Principe : Fusion des routes individuelles en calculant le gain
    kilométrique de chaque regroupement (Savings).
  - Usage : Le standard industriel pour minimiser le coût total de la flotte.

4. K-Means (Clustering IA) :
  - Principe : Regroupement automatique des clients en secteurs
    géographiques naturelles (Machine Learning).
  - Usage : Idéal pour adapter la flotte à la densité de population.

------------------------------------------------------------------------
5. INSTALLATION ET LANCEMENT
------------------------------------------------------------------------

Lancement du Backend (Spring Boot) :
1. Configurer PostgreSQL dans `src/main/resources/application.properties`.
2. Exécuter : `mvn spring-boot:run`
3. Le serveur écoute sur http://localhost:8080.
4. Execution de la matrice-cimplete.sql sur notre BD(commende à utiliser : docker-compose -f docker-compose-local.yml down -v ; docker-compose -f docker-compose-local.yml up -d (tous effectué dans le dossier docker) pui on execute matrice-completes.sql tout en choisissant la bd en question(l'execution dure entre 1 et 3 min le temps de remplir les données de la matrice ainsi que des adresses, des commandes etc .. dans la base de données)  )

Lancement du Frontend (Angular) :
1. Aller dans le dossier `client`.
2. Installer les dépendances : `npm install`
3. Lancer en mode développement : `npm start`
4. Accéder à l'interface sur http://localhost:4200.

Déploiement en Production (Mode VM) :
1. Compiler le front : `npm run build`
2. Copier le contenu de `dist/browser/` vers `src/main/resources/static/`
   du backend.
3. Lancer le backend : l'application est alors accessible sur le port 8080.

------------------------------------------------------------------------
6. DEPLOIEMENT & MAINTENANCE (DOCKER)
------------------------------------------------------------------------
L'application est prête pour un déploiement conteneurisé :
- Docker-compose : Orchestration du backend et de la base PostgreSQL.
  automatiquement au démarrage du conteneur DB.
- Automatisation : Script Cron configuré pour réinitialiser la base de
  données chaque jour à 00h00 via `docker-compose down -v`.

------------------------------------------------------------------------
*
