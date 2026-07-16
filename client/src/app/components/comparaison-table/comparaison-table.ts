import {Component, input, signal} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import {AlgoResult} from '../../services/planification.service';

type Motorisation = 'diesel' | 'essence' | 'electrique';

interface ProfilEnergie {
  label: string;
  unite: string;
  consommation100Km: number;
  prixUnite: number;
  co2KgParUnite: number;
}

@Component({
  selector: 'app-comparaison-table',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './comparaison-table.html',
  styleUrl: './comparaison-table.css',
})
export class ComparaisonTable {


  public gridResult = input<AlgoResult | null>(null);
  public greedyResult = input<AlgoResult | null>(null);
  public CWResult = input<AlgoResult | null>(null);
  public KMResult = input<AlgoResult | null>(null);

  public motorisation = signal<Motorisation>('diesel');
  public prixSaisi = signal<number>(1.9);

  private readonly profils: Record<Motorisation, ProfilEnergie> = {
    diesel: {
      label: 'Diesel',
      unite: 'L',
      consommation100Km: 8,
      prixUnite: 1.9,
      co2KgParUnite: 2.68,
    },
    essence: {
      label: 'Essence',
      unite: 'L',
      consommation100Km: 7,
      prixUnite: 1.9,
      co2KgParUnite: 2.31,
    },
    electrique: {
      label: 'Electrique',
      unite: 'kWh',
      consommation100Km: 18,
      prixUnite: 0.25,
      co2KgParUnite: 0.06,
    },
  };

  public setMotorisation(m: Motorisation): void {
    this.motorisation.set(m);
    this.prixSaisi.set(this.profils[m].prixUnite);
  }

  public onPrixChange(event: Event): void {
    const val = parseFloat((event.target as HTMLInputElement).value);
    if (!isNaN(val) && val >= 0) this.prixSaisi.set(val);
  }

  public profilActuel(): ProfilEnergie {
    return this.profils[this.motorisation()];
  }

  public energieConsommee(result: AlgoResult): number {
    return (result.distanceKm * this.profilActuel().consommation100Km) / 100;
  }

  public coutEnergie(result: AlgoResult): number {
    return this.energieConsommee(result) * this.prixSaisi();
  }

  public co2Kg(result: AlgoResult): number {
    return this.energieConsommee(result) * this.profilActuel().co2KgParUnite;
  }


}
