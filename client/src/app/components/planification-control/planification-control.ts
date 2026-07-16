import {Component, inject, input, output} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {AlgoResult, PlanificationService} from '../../services/planification.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-planification-control',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './planification-control.html',
  styleUrl: './planification-control.css'
})
export class PlanificationControlComponent {
  public planifService = inject(PlanificationService);
  // --- ENTRÉES (Signaux) ---
  public resultStandard = input<AlgoResult | null>(null);
  public resultGreedy = input<AlgoResult | null>(null);
  public resultCW = input<AlgoResult | null>(null);
  public resultKM = input<AlgoResult | null>(null);


  // --- SORTIES (Événements) ---
  public generateEvent = output<{date: string}>();
  public runGrid = output<{vehicles: number}>();
  public runGreedy = output<{vehicles: number}>();
  public runCW = output<{vehicles: number}>();
  public runKM = output<{vehicles: number}>();


  // --- ÉTAT DU FORMULAIRE ---

  public nbVehicles = 3;


  // On appelle une petite fonction maison pour avoir la date locale
  public selectedDate = this.getTodayLocalString();

  // Fonction qui récupère la vraie date française (YYYY-MM-DD)
  private getTodayLocalString(): string {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0'); //
    const dd = String(today.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  // --- MÉTHODES APPELÉES PAR LE HTML ---
  protected onLoadData() {
    if (this.selectedDate) {
      this.generateEvent.emit({ date: this.selectedDate });
    }
  }

  protected onRunGrid() {
    this.runGrid.emit({ vehicles: this.nbVehicles });
  }

  protected onRunGreedy() {
    this.runGreedy.emit({ vehicles: this.nbVehicles });
  }

  protected onRunClarkeAndWright() {
    this.runCW.emit({ vehicles: this.nbVehicles });
  }

  protected runKMeansStrategy() {
    this.runKM.emit({ vehicles: this.nbVehicles });
  }

}
