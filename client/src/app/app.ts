import { Component, inject, computed, signal } from '@angular/core';
import { PlanificationService, HistoriqueItem } from './services/planification.service';
import { PlanificationControlComponent } from './components/planification-control/planification-control';
import { LeafletModule } from '@bluehalo/ngx-leaflet';
import { polyline, tileLayer, Layer, Map, LatLngTuple } from 'leaflet';
import { getCircleMarker, getDepotMarker } from './utils/marker';
import { CommonModule } from '@angular/common';
import { ComparaisonTable } from './components/comparaison-table/comparaison-table';
import { Adresse } from './data/adresse';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [PlanificationControlComponent, LeafletModule, CommonModule, ComparaisonTable],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  public planif = inject(PlanificationService);
  public options = { zoom: 12, center: [45.188, 5.724] as any };

  // --- LE SYSTÈME D'ONGLETS ---
  public vueActuelle = signal<'carte' | 'historique'>('carte');

  constructor() {
    const today = new Date();
    const dateStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    this.planif.chargerAdressesDepuisBDD(dateStr);
  }

  // --- CALQUES POUR LA CARTE PRINCIPALE ---
  public layers = computed<Layer[]>(() => {
    return this.construireLayers(this.planif.adresses(), this.planif.currentRoutes());
  });

  // --- FONCTION PARTAGÉE POUR DESSINER N'IMPORTE QUELLE CARTE ---
  public construireLayers(adresses: readonly Adresse[], routes: ReadonlyArray<ReadonlyArray<LatLngTuple>>): Layer[] {
    const mapLayers: Layer[] = [ tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png') ];

    adresses.forEach((a, i) => {
      const isDepot = i === adresses.length - 1;
      mapLayers.push(isDepot
        ? getDepotMarker([a.lat, a.lng])
        : getCircleMarker([a.lat, a.lng], 'blue')
      );
    });

    const colors = ['red', 'blue', 'green', 'orange', 'purple', 'magenta', 'darkred', 'darkblue', 'darkgreen', 'cadetblue', 'chocolate', 'crimson', 'darkcyan', 'darkgoldenrod', 'darkviolet', 'deeppink', 'indigo', 'maroon', 'navy', 'olive', 'teal'];
    routes.forEach((r, i) => {
      mapLayers.push(polyline([...r], { color: colors[i % colors.length], weight: 4 }));
    });

    return mapLayers;
  }

  public onMapReady(map: Map): void {
    setTimeout(() => map.invalidateSize(), 200);
  }

  // --- BOUTONS DU PANNEAU GAUCHE ---
  public async handleGenerate(event: {date: string}) {
    await this.planif.chargerAdressesDepuisBDD(event.date);
  }
  public async handleGrid(event: {vehicles: number}) {
    await this.planif.runGridStrategy(event.vehicles, 100000);
  }
  public async handleGreedy(event: {vehicles: number}) {
    await this.planif.runGreedyStrategy(event.vehicles);
  }
  public async handleCW(event: {vehicles: number}){
    await this.planif.runClarkeAndWrightStrategy(event.vehicles);
  }

  protected async handleKM(event: {vehicles: number}){
    await this.planif.runKMeansStrategy(event.vehicles);
  }
}
