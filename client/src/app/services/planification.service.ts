import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Adresse } from '../data/adresse';
import { Carto } from './carto';
import { LatLngTuple } from 'leaflet';
import { CommandeResponse } from '../data/commande';

export interface AlgoResult {
  algoName: string; distanceKm: number; durationH: number; nbVehicules: number; nbPoints: number;savedTourneesIds?: number[];
}

export interface HistoriqueItem {
  id: string;
  algoName: string;
  distanceKm: number;
  durationH: number;
  nbVehicules: number;
  routes: ReadonlyArray<ReadonlyArray<LatLngTuple>>;
  adresses: readonly Adresse[];
}

@Injectable({ providedIn: 'root' })
export class PlanificationService {
  private carto = inject(Carto);
  private _httpClient = inject(HttpClient);

  public adresses = signal<readonly Adresse[]>([]);
  public currentRoutes = signal<ReadonlyArray<ReadonlyArray<LatLngTuple>>>([]);
  public resultStandard = signal<AlgoResult | null>(null);
  public resultGreedy = signal<AlgoResult | null>(null);
  public resultCW = signal<AlgoResult | null>(null);
  public resultKM = signal<AlgoResult | null>(null);
  public historique = signal<HistoriqueItem[]>([]);
  public uiMessage = signal<{text: string, type: 'error'|'info'|'success'} | null>(null);

  private async sauvegarderTournee(algorithme: string, distanceKm: number, tempsHeure: number, livraisonIds: number[]): Promise<number | null> {
    try {
      const request = {
        algorithme: algorithme,
        distanceKm: distanceKm,
        tempsHeure: tempsHeure,
        statut: "EN_COURS",
        journeeId: 1,
        livraisonIds: livraisonIds
      };

      const res = await firstValueFrom(this._httpClient.post<any>('/api/tournee', request));
      console.log(`Tournée ${algorithme} sauvegardée (ID: ${res.id})`);
      return res.id;
    } catch (e) {
      console.error("Erreur lors de la sauvegarde de la tournée :", e);
      return null;
    }
  }


  private sauvegarderMatriceEnBDD(points: any[], matrix: {distances: number[][], times: number[][]}) {

    const trajets = [];
    for (let i = 0; i < points.length; i++) {
      for (let j = 0; j < points.length; j++) {
        if (i !== j) { // On ne sauvegarde pas la distance d'un point vers lui-même
          trajets.push({
            latDepart: points[i].lat,
            lngDepart: points[i].lng,
            latArrivee: points[j].lat,
            lngArrivee: points[j].lng,
            distance: matrix.distances[i][j],
            temps: matrix.times[i][j]
          });
        }
      }
    }

    // On envoie le tableau au backend de manière asynchrone (sans bloquer l'interface)
    this._httpClient.post('/api/distances/save-batch', trajets).subscribe({
      next: () => console.log(` ${trajets.length} trajets ORS enregistrés dans le cache Spring Boot !`),
      error: (err) => console.warn('Erreur lors de la sauvegarde du cache (silencieux)', err)
    });
  }

  // ---------------------------------------------------------
  // 1. TÉLÉCHARGEMENT DEPUIS LA BDD ET SÉLECTION ALÉATOIRE
  // ---------------------------------------------------------
  public async chargerAdressesDepuisBDD(dateLivraison: string) {
    console.log(`\n --- TÉLÉCHARGEMENT DES COMMANDES DU ${dateLivraison} ---`);
    this.currentRoutes.set([]);
    this.resultStandard.set(null);
    this.resultGreedy.set(null);
    this.resultCW.set(null);
    this.resultKM.set(null);
    this.adresses.set([]);
    this.uiMessage.set({ text: "Récupération des commandes en cours...", type: 'info' });

    try {
      // 1. Appel HTTP vers le backend
      const req$ = this._httpClient.get<CommandeResponse[]>('/api/commandes', {
        params: { dateLivraisonSouhaitee: dateLivraison }
      });
      const commandes = await firstValueFrom(req$);

      // 2. Extraction des adresses uniques
      const adressesUniques: Adresse[] = [];
      const adressesDejaVues = new Set<number>();

      for (const cmd of commandes) {
        if (cmd.client?.adresse) {
          const adrBDD = cmd.client.adresse;
          if (!adressesDejaVues.has(adrBDD.id)) {
            adressesDejaVues.add(adrBDD.id);
            adressesUniques.push({
              lat: adrBDD.latitude,
              lng: adrBDD.longitude,
              name: adrBDD.rue,
              city: adrBDD.ville,
              postCode: ''
            });
          }
        }
      }

      if (adressesUniques.length === 0) {
        this.uiMessage.set({ text: "Aucune commande pour cette date.", type: 'info' });
        return;
      }

      // ---  RETOUR DU MÉLANGE ALÉATOIRE (TON ANCIEN CODE) ---
      // On mélange toute la liste pour que l'entrepôt et les clients changent à chaque clic
      const adressesMelangees = [...adressesUniques].sort(() => Math.random() - 0.5);

      // On définit l'entrepôt comme étant le premier de la liste mélangée
      const depot = adressesMelangees[0];
      // Le reste sont des clients potentiels
      const resteDesClients = adressesMelangees.slice(1);

      // ---  GESTION DES LIMITES (CODE COLLÈGUES) ---
      const dateCible = new Date(dateLivraison);
      dateCible.setHours(0,0,0,0);
      const dateAujourdhui = new Date();
      dateAujourdhui.setHours(0,0,0,0);

      const diffTime = dateCible.getTime() - dateAujourdhui.getTime();
      const diffJours = Math.round(diffTime / (1000 * 3600 * 24));

      // Si c'est dans plus de 2 jours, on limite à 100, sinon 400 (pour ton gros cache)
      const limiteClients = diffJours >= 2 ? 100 : 400;

      // On prend les N premiers clients après le mélange
      const clientsAffiches = resteDesClients.slice(0, limiteClients);

      // --- ASSEMBLAGE FINAL ---
      // On met les clients d'abord et le DEPOT EN DERNIER (important pour tes algos)
      const adressesAffichees = [...clientsAffiches, depot];

      this.adresses.set(adressesAffichees);

      console.log(` Succès : ${commandes.length} commandes récupérées.`);
      console.log(` Sélection Aléatoire : ${clientsAffiches.length} clients + 1 Entrepôt.`);

      this.uiMessage.set({ text: `${clientsAffiches.length} clients chargés aléatoirement pour le ${dateLivraison}`, type: 'success' });
      setTimeout(() => this.uiMessage.set(null), 3000);

    } catch (e) {
      console.error(` Erreur de connexion au Backend Spring Boot :`, e);
      this.uiMessage.set({ text: "Erreur de connexion (Le backend est-il lancé ?)", type: 'error' });
    }
  }
  // ---------------------------------------------------------
  // 2. ALGO GRID STRICT
  // ---------------------------------------------------------
  public async runGridStrategy(nbVehicules: number, maxTime: number) {
    const toutesLesAdresses = this.adresses();
    if (toutesLesAdresses.length < 2) return;

    // --- NETTOYAGE DES ANCIENS TRACÉS ---
    this.currentRoutes.set([]);       // Efface les traits sur la carte

    // -----

    console.log(`\n --- DÉBUT ALGORITHME GRID STRICT (SWEEP) ---`);
    console.log(` Points totaux : ${toutesLesAdresses.length} |  Véhicules FORCÉS : ${nbVehicules}`);
    this.uiMessage.set({ text: "Calcul Grid Strict en cours...", type: 'info' });

    const depot = toutesLesAdresses[toutesLesAdresses.length - 1];
    const clients = [...toutesLesAdresses.slice(0, -1)];


    clients.sort((a, b) => {
      const angleA = Math.atan2(a.lat - depot.lat, a.lng - depot.lng);
      const angleB = Math.atan2(b.lat - depot.lat, b.lng - depot.lng);
      return angleA - angleB;
    });

    const nbClientsParVehicule = Math.ceil(clients.length / nbVehicules);
    const allRoutes: ReadonlyArray<LatLngTuple>[] = [];
    let totalDist = 0, totalTime = 0;

    for (let v = 0; v < nbVehicules; v++) {
      const clientsDuVehicule = clients.slice(v * nbClientsParVehicule, (v + 1) * nbClientsParVehicule);
      if (clientsDuVehicule.length === 0) continue;

      console.log(` Véhicule ${v + 1} : Attribution de ${clientsDuVehicule.length} clients.`);

      const paquets = [];
      for (let i = 0; i < clientsDuVehicule.length; i += 35) {
        paquets.push(clientsDuVehicule.slice(i, i + 35));
      }

      const routeCoords: [number, number][] = [];
      let parkingDepart = depot;

      for (let p = 0; p < paquets.length; p++) {
        try {
          console.log(`    ORS Optimize en cours pour le paquet ${p+1}/${paquets.length}...`);
          await new Promise(r => setTimeout(r, 1000));
          const opt = await this.carto.optimize({
            nbVehicules: 1, maxTimePerVehicule: maxTime, adresses: paquets[p], parking: parkingDepart
          });

          console.log(`    Optimisation réussie pour le paquet ${p+1}.`);
          const route = opt.routes[0];


          totalDist += route.cost / 100;
          totalTime += route.duration / 3600;

          routeCoords.push(...route.steps.map(s => s.location as [number, number]));
          const lastStep = route.steps[route.steps.length - 1].location;
          parkingDepart = { lng: lastStep[0], lat: lastStep[1] } as Adresse;
        } catch(e) {
          console.error(` Erreur ORS sur le paquet ${p+1} du véhicule ${v+1}`, e);
        }
      }

      routeCoords.push([depot.lng, depot.lat]);

      console.log(`    Dessin de la route GPS pour le véhicule ${v + 1}...`);
      let routeDessinee: LatLngTuple[] = [];
      for (let i = 0; i < routeCoords.length - 1; i += 40) {
        const segment = routeCoords.slice(i, i + 41);
        try {
          await new Promise(r => setTimeout(r, 500));
          routeDessinee.push(...await this.carto.getDirections(segment));
        } catch {
          console.warn(`    Tracé GPS introuvable pour la portion globale. Résolution point par point en cours...`);
          // SOLUTION IMPLÉMENTÉE ICI : Résolution par paire
          for (let j = 0; j < segment.length - 1; j++) {
            const pair = [segment[j], segment[j+1]];
            try {
              await new Promise(r => setTimeout(r, 800)); // Délai de 800ms pour éviter le blocage de l'API
              routeDessinee.push(...await this.carto.getDirections(pair));
            } catch {
              console.warn(`      Tracé impossible entre le point ${j} et ${j+1}. Ligne droite utilisée uniquement ici.`);
              routeDessinee.push([pair[0][1], pair[0][0]] as LatLngTuple); // Point A
              routeDessinee.push([pair[1][1], pair[1][0]] as LatLngTuple); // Point B en ligne droite
            }
          }
        }
      }
      allRoutes.push(routeDessinee);
      console.log(` Véhicule ${v + 1} terminé.`);
    }

    this.currentRoutes.set(allRoutes);

    // Sauvegarde en base
    const savedId = await this.sauvegarderTournee("GRID", totalDist, totalTime, []);

    this.resultStandard.set({
      algoName: "Grid Strict",
      distanceKm: totalDist,
      durationH: totalTime,
      nbVehicules: allRoutes.length,
      nbPoints: clients.length,
      savedTourneesIds: savedId ? [savedId] : []
    });

    this.historique.update(h => [{ id: crypto.randomUUID(), algoName: "Grid Strict", distanceKm: totalDist, durationH: totalTime, nbVehicules: allRoutes.length, routes: allRoutes, adresses: toutesLesAdresses }, ...h]);
    console.log(` BILAN GRID : ${allRoutes.length} routes créées | Dist: ${totalDist.toFixed(2)} km | Temps roulage: ${totalTime.toFixed(2)} h.`);
    this.uiMessage.set({ text: "Grid Strict terminé et sauvegardé !", type: 'success' });
  }

  // ---------------------------------------------------------
  // 3. ALGO GREEDY STRICT (Sweep Algorithm)
  // ---------------------------------------------------------
  public async runGreedyStrategy(nbVehicules: number) {
    const toutesLesAdresses = this.adresses();
    if (toutesLesAdresses.length < 2) return;
    // --- NETTOYAGE DES ANCIENS TRACÉS ---
    this.currentRoutes.set([]);       // Efface les traits sur la carte



    console.log(`\n --- DÉBUT ALGORITHME GREEDY STRICT (SWEEP) ---`);
    console.log(` Points totaux : ${toutesLesAdresses.length} |  Véhicules FORCÉS : ${nbVehicules}`);
    this.uiMessage.set({ text: "Calcul Greedy Strict en cours...", type: 'info' });

    const depot = toutesLesAdresses[toutesLesAdresses.length - 1];
    const clients = [...toutesLesAdresses.slice(0, -1)];


    clients.sort((a, b) => {
      const angleA = Math.atan2(a.lat - depot.lat, a.lng - depot.lng);
      const angleB = Math.atan2(b.lat - depot.lat, b.lng - depot.lng);
      return angleA - angleB;
    });

    const nbClientsParVehicule = Math.ceil(clients.length / nbVehicules);
    const allRoutes: ReadonlyArray<LatLngTuple>[] = [];
    let totalDist = 0, totalTime = 0;

    for (let v = 0; v < nbVehicules; v++) {
      const clientsDuVehicule = clients.slice(v * nbClientsParVehicule, (v + 1) * nbClientsParVehicule);
      if (clientsDuVehicule.length === 0) continue;

      console.log(` Véhicule ${v + 1} : Attribution de ${clientsDuVehicule.length} clients.`);

      const paquets = [];
      for (let i = 0; i < clientsDuVehicule.length; i += 40) {
        paquets.push(clientsDuVehicule.slice(i, i + 40));
      }

      const routeCoords: [number, number][] = [[depot.lng, depot.lat]];
      let pointActuel = depot;

      for (let p = 0; p < paquets.length; p++) {
        const paquet = paquets[p];
        const pointsAPI = [pointActuel, ...paquet];

        try {
          console.log(`    Recherche de la Matrice pour le paquet ${p+1}/${paquets.length}...`);
          //await new Promise(r => setTimeout(r, 1000));

          // --- APPEL DE LA MÉTHODE ---
          const matrixResult = await this.carto.getDistanceMatrix(pointsAPI);
          console.log(`    Matrice récupérée ! Calcul glouton local en cours...`);

          // --- SAUVEGARDE CONDITIONNELLE ---
          if (!matrixResult.fromCache) {
            this.sauvegarderMatriceEnBDD(pointsAPI, matrixResult);
          }

          // On recrée l'objet matrix pour que la suite de votre algo fonctionne sans rien changer
          const matrix = { distances: matrixResult.distances, times: matrixResult.times };

          const unassigned = new Set(Array.from({length: paquet.length}, (_, idx) => idx + 1));
          let currentLocIdx = 0;

          while(unassigned.size > 0) {
            let bestNext = -1; let minTime = Infinity;

            for(const job of unassigned) {
              const time = matrix.times[currentLocIdx][job];
              if(time < minTime) {
                minTime = time;
                bestNext = job;
              }
            }

            if(bestNext !== -1) {
              const addr = pointsAPI[bestNext];
              routeCoords.push([addr.lng, addr.lat]);


              totalTime += minTime / 3600;

              const d = matrix.distances[currentLocIdx][bestNext];
              totalDist += (d > 100) ? d / 1000 : d;

              unassigned.delete(bestNext);
              currentLocIdx = bestNext;
              pointActuel = addr;
            }
          }


          const dReturn = matrix.distances[currentLocIdx][0];
          totalDist += (dReturn > 100) ? dReturn / 1000 : dReturn;

          const tReturn = matrix.times[currentLocIdx][0];
          totalTime += tReturn / 3600;

        } catch(e) {
          console.error(` Erreur Matrice sur le paquet ${p+1}`, e);
        }
      }

      routeCoords.push([depot.lng, depot.lat]);

      console.log(`    Dessin de la route GPS pour le véhicule ${v + 1}...`);
      let routeDessinee: LatLngTuple[] = [];
      for(let i = 0; i < routeCoords.length - 1; i += 40) {
        const segment = routeCoords.slice(i, i + 41);
        try {
          await new Promise(r => setTimeout(r, 250));
          routeDessinee.push(...await this.carto.getDirections(segment));
        } catch {
          console.warn(`    Tracé GPS introuvable pour la portion globale. Résolution point par point en cours...`);
          //  Résolution par paire
          for (let j = 0; j < segment.length - 1; j++) {
            const pair = [segment[j], segment[j+1]];
            try {
              await new Promise(r => setTimeout(r, 100)); // Délai de 800ms pour éviter le blocage API
              routeDessinee.push(...await this.carto.getDirections(pair));
            } catch {
              console.warn(`      Tracé impossible entre le point ${j} et ${j+1}. Ligne droite utilisée uniquement ici.`);
              routeDessinee.push([pair[0][1], pair[0][0]] as LatLngTuple); // Point A
              routeDessinee.push([pair[1][1], pair[1][0]] as LatLngTuple); // Point B en ligne droite
            }
          }
        }
      }
      allRoutes.push(routeDessinee);
      console.log(` Véhicule ${v + 1} terminé.`);
    }

    this.currentRoutes.set(allRoutes);
    const savedId = await this.sauvegarderTournee("GREEDY", totalDist, totalTime, []);

    this.resultGreedy.set({
      algoName: "Greedy Strict",
      distanceKm: totalDist,
      durationH: totalTime,
      nbVehicules: allRoutes.length,
      nbPoints: clients.length,
      savedTourneesIds: savedId ? [savedId] : []
    });

    this.historique.update(h => [{ id: crypto.randomUUID(), algoName: "Greedy Strict", distanceKm: totalDist, durationH: totalTime, nbVehicules: allRoutes.length, routes: allRoutes, adresses: toutesLesAdresses }, ...h]);
    console.log(` BILAN GREEDY : ${allRoutes.length} routes créées | Dist: ${totalDist.toFixed(2)} km | Temps roulage: ${totalTime.toFixed(2)} h.`);
    this.uiMessage.set({ text: "Greedy Strict terminé et sauvegardé !", type: 'success' });
  }
  // ---------------------------------------------------------
  // 4. ALGO CLARKE & WRIGHT (Méthode des Économies)
  // ---------------------------------------------------------
  public async runClarkeAndWrightStrategy(nbVehiculesAttendus: number) {
    const toutesLesAdresses = this.adresses();
    if (toutesLesAdresses.length < 2) return;

    this.currentRoutes.set([]);
    console.log(`\n --- DÉBUT ALGORITHME CLARKE & WRIGHT ---`);
    console.log(` Points totaux : ${toutesLesAdresses.length} | Véhicules CIBLÉS : ${nbVehiculesAttendus}`);
    this.uiMessage.set({ text: "Calcul Clarke & Wright en cours...", type: 'info' });

    // Le dépôt est le dernier point de la liste (selon ta logique)
    const depot = toutesLesAdresses[toutesLesAdresses.length - 1];
    const clients = [...toutesLesAdresses.slice(0, -1)];
    const pointsAPI = [depot, ...clients];

    let totalDist = 0;
    let totalTime = 0;
    const allRoutes: ReadonlyArray<LatLngTuple>[] = [];

    try {
      // 1. Appel API pour la matrice de TOUS les points
      console.log(` Récupération de la Matrice Globale...`);
      //await new Promise(r => setTimeout(r, 1000));
      const matrixResult = await this.carto.getDistanceMatrix(pointsAPI);

      // Sauvegarde en cache si besoin
      if (!matrixResult.fromCache) {
        this.sauvegarderMatriceEnBDD(pointsAPI, matrixResult);
      }
      const matrix = { distances: matrixResult.distances, times: matrixResult.times };

      //  Calcul des Économies (Savings)
      console.log(` Calcul des économies...`);
      const savings: { clientI: number, clientJ: number, saving: number }[] = [];

      // La matrice place le dépôt à l'index 0. Les clients vont de 1 à N.
      for (let i = 1; i <= clients.length; i++) {
        for (let j = i + 1; j <= clients.length; j++) {
          const distDepotI = matrix.distances[0][i];
          const distDepotJ = matrix.distances[0][j];
          const distIJ = matrix.distances[i][j];

          // Formule de base de C&W : Économie de distance pure
          const economie = distDepotI + distDepotJ - distIJ;
          savings.push({ clientI: i, clientJ: j, saving: economie });
        }
      }

      //  Tri des économies (Décroissant : on veut les plus grosses économies d'abord)
      savings.sort((a, b) => b.saving - a.saving);

      //  Initialisation : Chaque client a sa propre route au départ
      // Une route est un tableau d'index de clients (ex: [1], [2]...)
      let routes: number[][] = [];
      for (let i = 1; i <= clients.length; i++) {
        routes.push([i]);
      }
      // On calcule la limite max pour équilibrer les camions (ex: 400 / 10 = 40 colis max)
      const maxClientsParVehicule = Math.ceil(clients.length / nbVehiculesAttendus)+5;
      // 5. La Fusion !
      console.log(` Fusion des routes en cours...`);
      for (const s of savings) {
        if (routes.length <= nbVehiculesAttendus) break; // On arrête si on a le bon nombre de camions

        const idxI = routes.findIndex(r => r.includes(s.clientI));
        const idxJ = routes.findIndex(r => r.includes(s.clientJ));

        // On ignore si les clients sont déjà dans la même tournée
        if (idxI === idxJ) continue;

        const routeI = routes[idxI];
        const routeJ = routes[idxJ];


        // Si la fusion des deux routes dépasse la capacité du camion, on annule cette fusion !
        if (routeI.length + routeJ.length > maxClientsParVehicule) continue;

        // On vérifie si les points sont aux extrémités
        const isIEnd = routeI[routeI.length - 1] === s.clientI;
        const isIStart = routeI[0] === s.clientI;
        const isJEnd = routeJ[routeJ.length - 1] === s.clientJ;
        const isJStart = routeJ[0] === s.clientJ;

        // Si l'un des deux points n'est pas à une extrémité, on ne peut pas fusionner par ce lien
        if (!isIEnd && !isIStart) continue;
        if (!isJEnd && !isJStart) continue;

        let nouvelleRoute: number[] = [];

        // Logique d'assemblage (il y a 4 cas possibles pour assembler les bouts de routes)
        if (isIEnd && isJStart) {
          nouvelleRoute = [...routeI, ...routeJ]; // RouteI -> RouteJ
        } else if (isJEnd && isIStart) {
          nouvelleRoute = [...routeJ, ...routeI]; // RouteJ -> RouteI
        } else if (isIEnd && isJEnd) {
          nouvelleRoute = [...routeI, ...routeJ.reverse()]; // RouteI -> (RouteJ à l'envers)
        } else if (isIStart && isJStart) {
          nouvelleRoute = [...routeI.reverse(), ...routeJ]; // (RouteI à l'envers) -> RouteJ
        }

        // On supprime les anciennes routes et on ajoute la nouvelle fusionnée
        routes = routes.filter((_, idx) => idx !== idxI && idx !== idxJ);
        routes.push(nouvelleRoute);
      }

      console.log(` Résultat CW : ${routes.length} routes générées.`);

      //  Calcul des distances finales et tracé GPS
      for (let v = 0; v < routes.length; v++) {
        const routeActuelle = routes[v];

        // On construit le tableau de coordonnées brutes [Dépôt -> ClientA -> ClientB -> Dépôt]
        const routeCoords: [number, number][] = [[depot.lng, depot.lat]];

        // Calcul manuel du temps et de la distance grâce à la matrice
        let lastIndex = 0; // Le dépôt
        for (const clientIdx of routeActuelle) {
          const addr = pointsAPI[clientIdx];
          routeCoords.push([addr.lng, addr.lat]);

          const d = matrix.distances[lastIndex][clientIdx];
          totalDist += (d > 100) ? d / 1000 : d;
          totalTime += matrix.times[lastIndex][clientIdx] / 3600;

          lastIndex = clientIdx;
        }

        // Retour au dépôt
        routeCoords.push([depot.lng, depot.lat]);
        const dReturn = matrix.distances[lastIndex][0];
        totalDist += (dReturn > 100) ? dReturn / 1000 : dReturn;
        totalTime += matrix.times[lastIndex][0] / 3600;

        // Tracé visuel sur la carte (On utilise ta logique de découpage par 40 pour l'API directions)
        console.log(`    Dessin de la route GPS pour le véhicule ${v + 1}...`);
        let routeDessinee: LatLngTuple[] = [];
        for(let i = 0; i < routeCoords.length - 1; i += 40) {
          const segment = routeCoords.slice(i, i + 41);
          try {
            await new Promise(r => setTimeout(r, 250));
            routeDessinee.push(...await this.carto.getDirections(segment));
          } catch {
            console.warn(`    Tracé GPS global échoué. Résolution point par point...`);
            for (let j = 0; j < segment.length - 1; j++) {
              const pair = [segment[j], segment[j+1]];
              try {
                await new Promise(r => setTimeout(r, 150));
                routeDessinee.push(...await this.carto.getDirections(pair));
              } catch {
                routeDessinee.push([pair[0][1], pair[0][0]] as LatLngTuple);
                routeDessinee.push([pair[1][1], pair[1][0]] as LatLngTuple);
              }
            }
          }
        }
        allRoutes.push(routeDessinee);
      }

      // 7. Affichage et Sauvegarde
      this.currentRoutes.set(allRoutes);

      // On sauvegarde en base via  Endpoint
      const savedId = await this.sauvegarderTournee("CLARKE_WRIGHT", totalDist, totalTime, []);

      this.resultCW.set({
        algoName: "Clarke & Wright",
        distanceKm: totalDist,
        durationH: totalTime,
        nbVehicules: allRoutes.length,
        nbPoints: clients.length,
        savedTourneesIds: savedId ? [savedId] : []
      });

      this.historique.update(h => [{ id: crypto.randomUUID(), algoName: "Clarke & Wright", distanceKm: totalDist, durationH: totalTime, nbVehicules: allRoutes.length, routes: allRoutes, adresses: toutesLesAdresses }, ...h]);

      console.log(` BILAN C&W : ${allRoutes.length} routes | Dist: ${totalDist.toFixed(2)} km | Temps: ${totalTime.toFixed(2)} h.`);
      this.uiMessage.set({ text: "Clarke & Wright terminé et sauvegardé !", type: 'success' });

    } catch (e) {
      console.error("Erreur CW Matrix :", e);
      this.uiMessage.set({ text: "Erreur lors du calcul C&W.", type: 'error' });
    }

  }

  // ---------------------------------------------------------
  // 5. ALGO K-Means (Cluster first, Route second)
  // ---------------------------------------------------------
  private calculateDistance(a: Adresse, b: Adresse): number {
    const lat1 = a.lat * Math.PI / 180;
    const lat2 = b.lat * Math.PI / 180;
    const dLat = (b.lat - a.lat) * Math.PI / 180;
    const dLng = (b.lng - a.lng) * Math.PI / 180;
    const A = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const C = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1 - A));
    return 6371 * C; // Rayon Terre en km (Distance à vol d'oiseau pour le clustering)
  }

  private calculateCentroid(points: Adresse[]): Adresse {
    const lat = points.reduce((sum, p) => sum + p.lat, 0) / points.length;
    const lng = points.reduce((sum, p) => sum + p.lng, 0) / points.length;
    return { lat, lng, name: 'centroide', city: '', postCode: '' };
  }

  public async runKMeansStrategy(nbVehicules: number) {
    const toutesLesAdresses = this.adresses();
    if (toutesLesAdresses.length < 2) return;

    this.currentRoutes.set([]);

    console.log(`\n --- DÉBUT ALGORITHME K-MEANS ---`);
    console.log(` Points totaux : ${toutesLesAdresses.length} | Véhicules FORCÉS : ${nbVehicules}`);
    this.uiMessage.set({ text: "Calcul K-Means en cours...", type: 'info' });

    const depot = toutesLesAdresses[toutesLesAdresses.length - 1];
    const clients = [...toutesLesAdresses.slice(0, -1)];

    // --- PHASE 1 : INITIALISATION K-MEANS ---
    let centroides: Adresse[] = [];
    const indicesSelected = new Set<number>();
    for (let i = 0; i < Math.min(nbVehicules, clients.length); i++) {
      let idx: number;
      do {
        idx = Math.floor(Math.random() * clients.length);
      } while (indicesSelected.has(idx));
      indicesSelected.add(idx);
      centroides.push({ ...clients[idx] });
    }

    // --- PHASE 2 : ITÉRATION K-MEANS (Formation des zones) ---
    const maxIterations = 5;
    let clusters: Adresse[][] = [];

    for (let iter = 0; iter < maxIterations; iter++) {
      clusters = Array.from({ length: centroides.length }, () => []);
      for (const client of clients) {
        let minDist = Infinity;
        let closestCentroidIdx = 0;
        for (let c = 0; c < centroides.length; c++) {
          const dist = this.calculateDistance(client, centroides[c]);
          if (dist < minDist) {
            minDist = dist;
            closestCentroidIdx = c;
          }
        }
        clusters[closestCentroidIdx].push(client);
      }

      const newCentroides: Adresse[] = [];
      for (let c = 0; c < clusters.length; c++) {
        if (clusters[c].length > 0) {
          newCentroides.push(this.calculateCentroid(clusters[c]));
        }
      }

      let hasConverged = true;
      for (let c = 0; c < centroides.length; c++) {
        if (this.calculateDistance(centroides[c], newCentroides[c]) > 0.1) {
          hasConverged = false;
          break;
        }
      }
      centroides = newCentroides;
      if (hasConverged) break;
    }

    // Recluster final pour s'assurer que personne n'est oublié
    clusters = Array.from({ length: centroides.length }, () => []);
    for (const client of clients) {
      let minDist = Infinity;
      let closestCentroidIdx = 0;
      for (let c = 0; c < centroides.length; c++) {
        const dist = this.calculateDistance(client, centroides[c]);
        if (dist < minDist) {
          minDist = dist;
          closestCentroidIdx = c;
        }
      }
      clusters[closestCentroidIdx].push(client);
    }

    // --- PHASE 3 : OPTIMISATION PAR CLUSTER (Avec TA Matrice Backend) ---
    const allRoutes: ReadonlyArray<LatLngTuple>[] = [];
    let totalDist = 0;
    let totalTime = 0;

    for (let v = 0; v < clusters.length; v++) {
      if (clusters[v].length === 0) continue;

      console.log(` Véhicule ${v + 1} : Routage de ${clusters[v].length} clients dans la zone...`);

      const clientsDuCluster = clusters[v];
      const paquets = [];
      for (let i = 0; i < clientsDuCluster.length; i += 40) {
        paquets.push(clientsDuCluster.slice(i, i + 40));
      }

      const routeCoords: [number, number][] = [[depot.lng, depot.lat]];
      let pointActuel = depot;

      for (let p = 0; p < paquets.length; p++) {
        const paquet = paquets[p];
        const pointsAPI = [pointActuel, ...paquet];

        try {
          // L'APPEL MAGIQUE : Utilise ta matrice BDD pour router l'intérieur du cluster
          const matrixResult = await this.carto.getDistanceMatrix(pointsAPI);

          if (!matrixResult.fromCache) {
            this.sauvegarderMatriceEnBDD(pointsAPI, matrixResult);
          }
          const matrix = { distances: matrixResult.distances, times: matrixResult.times };

          const unassigned = new Set(Array.from({ length: paquet.length }, (_, idx) => idx + 1));
          let currentLocIdx = 0;

          while (unassigned.size > 0) {
            let bestNext = -1;
            let minTime = Infinity;

            for (const job of unassigned) {
              const time = matrix.times[currentLocIdx][job];
              if (time < minTime) {
                minTime = time;
                bestNext = job;
              }
            }

            if (bestNext !== -1) {
              const addr = pointsAPI[bestNext];
              routeCoords.push([addr.lng, addr.lat]);
              totalTime += minTime / 3600;
              const d = matrix.distances[currentLocIdx][bestNext];
              totalDist += (d > 100) ? d / 1000 : d;
              unassigned.delete(bestNext);
              currentLocIdx = bestNext;
              pointActuel = addr;
            }
          }

          // Retour au dépôt
          const dReturn = matrix.distances[currentLocIdx][0];
          totalDist += (dReturn > 100) ? dReturn / 1000 : dReturn;
          totalTime += matrix.times[currentLocIdx][0] / 3600;

        } catch (e) {
          console.error(` Erreur Matrice sur le cluster ${v + 1}`, e);
        }
      }

      routeCoords.push([depot.lng, depot.lat]);

      // Dessin sur la carte Leaflet
      let routeDessinee: LatLngTuple[] = [];
      for (let i = 0; i < routeCoords.length - 1; i += 40) {
        const segment = routeCoords.slice(i, i + 41);
        try {
          await new Promise(r => setTimeout(r, 250));
          routeDessinee.push(...await this.carto.getDirections(segment));
        } catch {
          for (let j = 0; j < segment.length - 1; j++) {
            const pair = [segment[j], segment[j + 1]];
            try {
              await new Promise(r => setTimeout(r, 100));
              routeDessinee.push(...await this.carto.getDirections(pair));
            } catch {
              routeDessinee.push([pair[0][1], pair[0][0]] as LatLngTuple);
              routeDessinee.push([pair[1][1], pair[1][0]] as LatLngTuple);
            }
          }
        }
      }
      allRoutes.push(routeDessinee);
    }

    this.currentRoutes.set(allRoutes);

    // On utilise bien resultKMeans et on a enlevé le resultStandard.set()
    const savedId = await this.sauvegarderTournee("K-MEANS", totalDist, totalTime, []);
    this.resultKM.set({
      algoName: "K-Means",
      distanceKm: totalDist,
      durationH: totalTime,
      nbVehicules: allRoutes.length,
      nbPoints: clients.length,
      savedTourneesIds: savedId ? [savedId] : []
    });

    this.historique.update(h => [{ id: crypto.randomUUID(), algoName: "K-Means", distanceKm: totalDist, durationH: totalTime, nbVehicules: allRoutes.length, routes: allRoutes, adresses: toutesLesAdresses }, ...h]);

    console.log(` BILAN K-MEANS : ${allRoutes.length} routes créées | Dist: ${totalDist.toFixed(2)} km | Temps: ${totalTime.toFixed(2)} h.`);
    this.uiMessage.set({ text: "K-Means terminé et sauvegardé !", type: 'success' });
  }


}
