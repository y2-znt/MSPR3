import { Component, Input, SimpleChanges } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { ChartConfiguration, ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

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

  areaChartData!: ChartData<'line'>;
  areaChartOptions!: ChartOptions<'line'>;

  ngOnChanges(changes: SimpleChanges): void {
    console.log("maladie", this.diseaseName);
    if (
      this.totalCases !== undefined &&
      this.totalDeaths !== undefined &&
      this.totalRecoveries !== undefined
    ) {
      this.updateChart();
    }
  }

  updateChart() {
    this.areaChartData = {
      labels: [`Statistiques ${this.diseaseName}`],
      datasets: [
        {
          label: 'Cas Confirmés',
          data: [
            this.totalCases
          ],
          fill: true,
          borderColor: 'rgba(75,192,192,1)',
          backgroundColor: 'rgba(75,192,192,0.2)',
          tension: 0.3
        },
        {
          label: 'Décès',
          data: [
            this.totalDeaths
          ],
          fill: true,
          borderColor: 'rgb(192, 93, 75)',
          backgroundColor: 'rgba(192, 174, 75, 0.2)',
          tension: 0.3
        },
        {
          label: 'Rétablis',
          data: [
            this.totalRecoveries
          ],
          fill: true,
          borderColor: 'rgb(75, 122, 192)',
          backgroundColor: 'rgba(75,192,192,0.2)',
          tension: 0.3
        }

      ]
    };

    this.areaChartOptions = {
      responsive: true,
      plugins: {
        legend: {
          display: true
        },
        tooltip: {
          mode: 'index',
          intersect: false
        }
      },
      scales: {
        y: {
          beginAtZero: true
        }
      }
    };
  }




  // Area Chart Configuration
  // areaChartData: ChartConfiguration<'line'>['data'] = {
  //   labels: Array.from({ length: 12 }, (_, i) => {
  //     const date = new Date();
  //     date.setMonth(date.getMonth() - (11 - i));
  //     return date.toLocaleDateString('fr-FR', {
  //       month: 'short',
  //       year: 'numeric',
  //     });
  //   }),
  //   datasets: [
  //     {
  //       label: 'Cas Totaux',
  //       data: [
  //         30000000, 35000000, 40000000, 45000000, 50000000, 55000000, 60000000,
  //         65000000, 70000000, 75000000, 80000000, 90000000,
  //       ],
  //       borderColor: '#2196f3',
  //       backgroundColor: 'rgba(33, 150, 243, 0.1)',
  //       fill: true,
  //       tension: 0.4,
  //     },
  //     {
  //       label: 'Guérisons',
  //       data: [
  //         25000000, 30000000, 35000000, 40000000, 45000000, 50000000, 55000000,
  //         60000000, 65000000, 70000000, 75000000, 85000000,
  //       ],
  //       borderColor: '#4caf50',
  //       backgroundColor: 'rgba(76, 175, 80, 0.1)',
  //       fill: true,
  //       tension: 0.4,
  //     },
  //     {
  //       label: 'Décès',
  //       data: [
  //         1000000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000,
  //         4500000, 5000000, 5500000, 6000000, 6500000,
  //       ],
  //       borderColor: '#f44336',
  //       backgroundColor: 'rgba(244, 67, 54, 0.1)',
  //       fill: true,
  //       tension: 0.4,
  //     },
  //   ],
  // };

  // areaChartOptions: ChartOptions<'line'> = {
  //   responsive: true,
  //   maintainAspectRatio: false,
  //   plugins: {
  //     legend: {
  //       position: 'top',
  //       align: 'end',
  //     },
  //     tooltip: {
  //       mode: 'index',
  //       intersect: false,
  //     },
  //   },
  //   scales: {
  //     x: {
  //       grid: {
  //         display: false,
  //       },
  //     },
  //     y: {
  //       beginAtZero: true,
  //       grid: {
  //         color: 'rgba(0, 0, 0, 0.1)',
  //       },
  //       ticks: {
  //         callback: function (tickValue: string | number) {
  //           const value = Number(tickValue);
  //           if (!isNaN(value) && value >= 1000000) {
  //             return (value / 1000000).toFixed(0) + 'M';
  //           }
  //           return tickValue;
  //         },
  //       },
  //     },
  //   },
  //   interaction: {
  //     mode: 'nearest',
  //     axis: 'x',
  //     intersect: false,
  //   },
  // };
}
