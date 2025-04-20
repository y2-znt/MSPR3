import { Component, Input, SimpleChanges } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { AggregatedDiseaseCase } from '../../../models/diseaseCase.model';
import { DiseaseCaseService } from '../../../services/disease-case.service';

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
export class OverviewComponent {
  @Input() totalCases!: number;
  @Input() totalDeaths!: number;
  @Input() totalRecoveries!: number;
  @Input() diseaseName!: string;

  timeSeriesData: AggregatedDiseaseCase[] = [];
  weeklyData: WeeklyData[] = [];
  areaChartData!: ChartData<'line'>;
  areaChartOptions!: ChartOptions<'line'>;

  constructor(private diseaseCaseService: DiseaseCaseService) {}

  ngOnInit() {
    this.loadTimeSeriesData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.weeklyData && this.weeklyData.length > 0) {
      this.updateChart();
    }
  }

  ngOnDestroy(): void {}

  loadTimeSeriesData() {
    this.diseaseCaseService.getAggregatedCasesByDate().subscribe({
      next: (data: AggregatedDiseaseCase[]) => {
        this.timeSeriesData = data;
        this.processWeeklyData();
        this.updateChart();
      },
      error: (error: any) => {
        console.error('Erreur lors de la récupération des données:', error);
      },
    });
  }

  processWeeklyData() {
    const sortedData = [...this.timeSeriesData].sort(
      (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
    );

    const weeklyMap = new Map<string, WeeklyData>();

    sortedData.forEach((dayData) => {
      const date = new Date(dayData.date);
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
        dayData.confirmedCases
      );
      weekData.deaths = Math.max(weekData.deaths, dayData.deaths);
      weekData.recovered = Math.max(weekData.recovered, dayData.recovered);
    });

    this.weeklyData = Array.from(weeklyMap.values());
  }

  updateChart() {
    this.areaChartData = {
      labels: this.weeklyData.map((week) => week.weekLabel),
      datasets: [
        {
          label: 'Confirmed Cases',
          data: this.weeklyData.map((week) => week.confirmedCases),
          fill: true,
          borderColor: 'rgba(75,192,192,1)',
          backgroundColor: 'rgba(75,192,192,0.2)',
          tension: 0.3,
        },
        {
          label: 'Deaths',
          data: this.weeklyData.map((week) => week.deaths),
          fill: true,
          borderColor: 'rgb(192, 93, 75)',
          backgroundColor: 'rgba(192, 174, 75, 0.2)',
          tension: 0.3,
        },
        {
          label: 'Recovered',
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
