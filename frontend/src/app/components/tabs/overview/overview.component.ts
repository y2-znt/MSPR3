import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AggregatedDiseaseCase } from '../../../models/diseaseCase.model';
import { DiseaseCaseService } from '../../../services/disease-case.service';
import { Country } from '../../../models/country.model';


interface WeeklyData {
  weekLabel: string;
  confirmedCases: number;
  deaths: number;
  recovered: number;
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [MatCardModule, BaseChartDirective],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
})
export class OverviewComponent implements OnInit, OnChanges, OnDestroy {
  @Input() totalCases!: number;
  @Input() totalDeaths!: number;
  @Input() totalRecoveries!: number;
  @Input() diseaseName!: string;
  @Input() selectedCountries: Country[] = [];
  @Input() dateStart: string | null = null;
  @Input() dateEnd: string | null = null;

  timeSeriesData: AggregatedDiseaseCase[] = [];
  weeklyData: WeeklyData[] = [];
  areaChartData!: ChartData<'line'>;
  areaChartOptions!: ChartOptions<'line'>;

  constructor(private diseaseCaseService: DiseaseCaseService) {}

  ngOnInit() {
    this.loadTimeSeriesData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('OverviewComponent changes:', changes);
    
    // Recharger les données si les pays sélectionnés ou les dates changent
    if (changes['selectedCountries'] || changes['dateStart'] || changes['dateEnd']) {
      console.log('Filter criteria changed, reloading data:', {
        countries: this.selectedCountries,
        dateStart: this.dateStart,
        dateEnd: this.dateEnd
      });
      this.loadTimeSeriesData();
    }
    
    // Mettre à jour le graphique si on a des données
    if (this.weeklyData && this.weeklyData.length > 0) {
      this.updateChart();
    }
  }

  ngOnDestroy(): void {}

  public getSelectedCountriesText(): string {
    if (!this.selectedCountries || this.selectedCountries.length === 0) {
      return 'Worldwide';
    }
    
    try {
      return this.selectedCountries
        .filter(country => country && country.name) // Filtrer les pays valides
        .map(country => country.name)
        .join(', ');
    } catch (error) {
      console.error('Error formatting country names:', error);
      return 'Worldwide';
    }
  }

  loadTimeSeriesData() {
    console.log('Loading time series data with filters:', {
      countries: this.selectedCountries,
      dateStart: this.dateStart,
      dateEnd: this.dateEnd
    });
    
    if (!this.selectedCountries || this.selectedCountries.length === 0) {
      console.log('No countries selected, fetching global data with date filter');
      this.diseaseCaseService.getAggregatedCasesByDate(this.dateStart || undefined, this.dateEnd || undefined).subscribe({
        next: (data: AggregatedDiseaseCase[]) => {
          console.log('Global data received:', data);
          this.timeSeriesData = data;
          this.processWeeklyData();
          this.updateChart();
        },
        error: (error: any) => {
          console.error('Error retrieving global data:', error);
        },
      });
    } else {
      console.log('Fetching data for selected countries with date filter');
      
      this.diseaseCaseService.getAggregatedCasesByDateAndCountries(
        this.selectedCountries,
        this.dateStart || undefined,
        this.dateEnd || undefined
      ).subscribe({
        next: (data: AggregatedDiseaseCase[]) => {
          console.log('Country data received:', data);
          
          if (!data || data.length === 0) {
            console.warn('No data received for selected filters');
            // Fallback to global data if no country data
            this.loadGlobalData();
            return;
          }
          
          this.timeSeriesData = data;
          this.processWeeklyData();
          this.updateChart();
        },
        error: (error: any) => {
          console.error('Error retrieving filtered data:', error);
          // Fallback to global data on error
          this.loadGlobalData();
        },
      });
    }
  }
  
  private loadGlobalData() {
    // Charger les données globales en conservant le filtre de date
    this.diseaseCaseService.getAggregatedCasesByDate(
      this.dateStart || undefined, 
      this.dateEnd || undefined
    ).subscribe({
      next: (data) => {
        this.timeSeriesData = data;
        this.processWeeklyData();
        this.updateChart();
      },
      error: (error) => console.error('Error loading global fallback data:', error)
    });
  }

  processWeeklyData() {
    if (!this.timeSeriesData || this.timeSeriesData.length === 0) {
      console.warn('No time series data available to process');
      this.weeklyData = [];
      return;
    }

    console.log('Processing weekly data, sample:', this.timeSeriesData[0]);
    
    const sortedData = [...this.timeSeriesData].sort(
      (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
    );

    const weeklyMap = new Map<string, WeeklyData>();

    sortedData.forEach((dayData) => {
      // Validation de la date
      if (!dayData.date) {
        console.warn('Skipping data point with missing date:', dayData);
        return;
      }

      const date = new Date(dayData.date);
      if (isNaN(date.getTime())) {
        console.warn('Invalid date format:', dayData.date);
        return;
      }

      const firstDayOfWeek = new Date(date);
      firstDayOfWeek.setDate(date.getDate() - date.getDay() + 1);
      const weekKey = firstDayOfWeek.toISOString().split('T')[0];

      if (!weeklyMap.has(weekKey)) {
        weeklyMap.set(weekKey, {
          weekLabel: `${firstDayOfWeek.toLocaleDateString('en-US', {
            day: '2-digit',
            month: 'short',
          })}`,
          confirmedCases: 0,
          deaths: 0,
          recovered: 0,
        });
      }

      const weekData = weeklyMap.get(weekKey)!;
      weekData.confirmedCases = Math.max(
        weekData.confirmedCases,
        dayData.confirmedCases || 0
      );
      weekData.deaths = Math.max(weekData.deaths, dayData.deaths || 0);
      weekData.recovered = Math.max(weekData.recovered, dayData.recovered || 0);
    });

    this.weeklyData = Array.from(weeklyMap.values());
    console.log('Processed weekly data points:', this.weeklyData.length);
  }

  updateChart() {
    // Générer le titre en incluant les dates si disponibles
    let chartTitle = 'Global';
    if (this.selectedCountries && this.selectedCountries.length > 0) {
      const countryNames = this.selectedCountries.map(c => c.name).join(', ');
      chartTitle = countryNames;
    }
    
    // Ajouter la plage de dates au titre si disponible
    let dateRange = '';
    if (this.dateStart && this.dateEnd) {
      dateRange = ` (${this.dateStart} to ${this.dateEnd})`;
    } else if (this.dateStart) {
      dateRange = ` (from ${this.dateStart})`;
    } else if (this.dateEnd) {
      dateRange = ` (until ${this.dateEnd})`;
    }
    
    this.areaChartData = {
      labels: this.weeklyData.map((week) => week.weekLabel),
      datasets: [
        {
          label: `Confirmed Cases (${chartTitle}${dateRange})`,
          data: this.weeklyData.map((week) => week.confirmedCases),
          fill: true,
          borderColor: 'rgba(75,192,192,1)',
          backgroundColor: 'rgba(75,192,192,0.2)',
          tension: 0.3,
        },
        {
          label: `Deaths (${chartTitle}${dateRange})`,
          data: this.weeklyData.map((week) => week.deaths),
          fill: true,
          borderColor: 'rgb(192, 93, 75)',
          backgroundColor: 'rgba(192, 174, 75, 0.2)',
          tension: 0.3,
        },
        {
          label: `Recovered (${chartTitle}${dateRange})`,
          data: this.weeklyData.map((week) => week.recovered),
          fill: true,
          borderColor: 'rgb(75, 122, 192)',
          backgroundColor: 'rgba(75,192,192,0.2)',
          tension: 0.3,
        },
      ],
    };

    this.areaChartOptions = {
      responsive: true,
      plugins: {
        legend: {
          display: true,
        },
        tooltip: {
          mode: 'index',
          intersect: false,
        },
      },
      scales: {
        y: {
          beginAtZero: true,
        },
        x: {
          ticks: {
            maxRotation: 45,
            minRotation: 45,
          },
        },
      },
    };
  }

}
