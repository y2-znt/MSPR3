import { CommonModule } from '@angular/common';
import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIcon } from '@angular/material/icon';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { Country } from '../../../models/country.model';
import { AggregatedDiseaseCase } from '../../../models/diseaseCase.model';
import { TranslatePipe } from '../../../pipes/translate.pipe';
import { DiseaseCaseService } from '../../../services/disease-case.service';
import { TranslationService } from '../../../services/translation.service';

interface WeeklyData {
  weekLabel: string;
  confirmedCases: number;
  deaths: number;
  recovered: number;
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    BaseChartDirective,
    MatButton,
    MatIcon,
    TranslatePipe,
  ],
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

  constructor(
    private diseaseCaseService: DiseaseCaseService,
    private translationService: TranslationService
  ) {}

  ngOnInit() {
    this.loadTimeSeriesData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['selectedCountries'] ||
      changes['dateStart'] ||
      changes['dateEnd']
    ) {
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
      return this.translationService.getInstantTranslation(
        'dashboard.chart.worldwide'
      );
    }

    try {
      return this.selectedCountries
        .filter((country) => country && country.name)
        .map((country) => country.name)
        .join(', ');
    } catch (error) {
      console.error('Error formatting country names:', error);
      return this.translationService.getInstantTranslation(
        'dashboard.chart.worldwide'
      );
    }
  }

  loadTimeSeriesData() {
    if (!this.selectedCountries || this.selectedCountries.length === 0) {
      this.diseaseCaseService
        .getAggregatedCasesByDate(
          this.dateStart || undefined,
          this.dateEnd || undefined
        )
        .subscribe({
          next: (data: AggregatedDiseaseCase[]) => {
            this.timeSeriesData = data;
            this.processWeeklyData();
            this.updateChart();
          },
          error: (error: any) => {
            console.error('Error retrieving global data:', error);
          },
        });
    } else {
      this.diseaseCaseService
        .getAggregatedCasesByDateAndCountries(
          this.selectedCountries,
          this.dateStart || undefined,
          this.dateEnd || undefined
        )
        .subscribe({
          next: (data: AggregatedDiseaseCase[]) => {
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
    // load data with date filters
    this.diseaseCaseService
      .getAggregatedCasesByDate(
        this.dateStart || undefined,
        this.dateEnd || undefined
      )
      .subscribe({
        next: (data) => {
          this.timeSeriesData = data;
          this.processWeeklyData();
          this.updateChart();
        },
        error: (error) =>
          console.error('Error loading global fallback data:', error),
      });
  }

  processWeeklyData() {
    if (!this.timeSeriesData || this.timeSeriesData.length === 0) {
      console.warn('No time series data available to process');
      this.weeklyData = [];
      return;
    }

    const sortedData = [...this.timeSeriesData].sort(
      (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
    );

    const weeklyMap = new Map<string, WeeklyData>();

    sortedData.forEach((dayData) => {
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
  }

  updateChart() {
    let chartTitle = 'Global';
    if (this.selectedCountries && this.selectedCountries.length > 0) {
      const countryNames = this.selectedCountries.map((c) => c.name).join(', ');
      chartTitle = countryNames;
    }

    let dateRange = '';
    if (this.dateStart && this.dateEnd) {
      dateRange = ` (${
        this.dateStart
      } ${this.translationService.getInstantTranslation(
        'dashboard.chart.to'
      )} ${this.dateEnd})`;
    } else if (this.dateStart) {
      dateRange = ` (${this.translationService.getInstantTranslation(
        'dashboard.chart.from'
      )} ${this.dateStart})`;
    } else if (this.dateEnd) {
      dateRange = ` (${this.translationService.getInstantTranslation(
        'dashboard.chart.to'
      )} ${this.dateEnd})`;
    }

    this.areaChartData = {
      labels: this.weeklyData.map((week) => week.weekLabel),
      datasets: [
        {
          label: `${this.translationService.getInstantTranslation(
            'dashboard.chart.cases'
          )} (${chartTitle}${dateRange})`,
          data: this.weeklyData.map((week) => week.confirmedCases),
          fill: true,
          borderColor: 'rgba(75,192,192,1)',
          backgroundColor: 'rgba(75,192,192,0.2)',
          tension: 0.3,
        },
        {
          label: `${this.translationService.getInstantTranslation(
            'dashboard.chart.deaths'
          )} (${chartTitle}${dateRange})`,
          data: this.weeklyData.map((week) => week.deaths),
          fill: true,
          borderColor: 'rgb(192, 93, 75)',
          backgroundColor: 'rgba(192, 174, 75, 0.2)',
          tension: 0.3,
        },
        {
          label: `${this.translationService.getInstantTranslation(
            'dashboard.chart.recovered'
          )} (${chartTitle}${dateRange})`,
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

  public exportToCSV(): void {
    if (!this.weeklyData || this.weeklyData.length === 0) {
      console.warn('No weekly data to export.');
      return;
    }

    const headers = ['Week', 'Confirmed Cases', 'Deaths', 'Recovered'];
    const rows = this.weeklyData.map((data) => [
      data.weekLabel,
      data.confirmedCases,
      data.deaths,
      data.recovered,
    ]);

    const csvContent = [headers, ...rows].map((e) => e.join(',')).join('\n');

    let fileName = 'global_weekly_data.csv';

    if (this.selectedCountries && this.selectedCountries.length === 1) {
      fileName = `${this.selectedCountries[0].name}_weekly_data.csv`;
    }

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.setAttribute('download', fileName);
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
