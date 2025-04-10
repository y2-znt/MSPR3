import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';

import { ChartConfiguration, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  providers: [provideNativeDateAdapter()],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatAutocompleteModule,
    MatCardModule,
    MatIconModule,
    MatButtonToggleModule,
    MatSelectModule,
    MatTabsModule,
    BaseChartDirective,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {
  // Form Controls
  dateRange = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  countriesControl = new FormControl<string[]>([]);
  countries: string[] = [
    'France',
    'Allemagne',
    'Italie',
    'Espagne',
    'Royaume-Uni',
    'États-Unis',
    'Chine',
    'Japon',
  ];

  // KPI Data
  totalCases: number = 675432198;
  totalDeaths: number = 6932408;
  mortalityRate: number = 1.03;
  totalRecoveries: number = 648392781;
  recoveryRate: number = 96.0;
  totalTests: number = 7023456789;

  // Area Chart Configuration
  areaChartData: ChartConfiguration<'line'>['data'] = {
    labels: Array.from({ length: 12 }, (_, i) => {
      const date = new Date();
      date.setMonth(date.getMonth() - (11 - i));
      return date.toLocaleDateString('fr-FR', {
        month: 'short',
        year: 'numeric',
      });
    }),
    datasets: [
      {
        label: 'Cas Totaux',
        data: [
          30000000, 35000000, 40000000, 45000000, 50000000, 55000000, 60000000,
          65000000, 70000000, 75000000, 80000000, 90000000,
        ],
        borderColor: '#2196f3',
        backgroundColor: 'rgba(33, 150, 243, 0.1)',
        fill: true,
        tension: 0.4,
      },
      {
        label: 'Guérisons',
        data: [
          25000000, 30000000, 35000000, 40000000, 45000000, 50000000, 55000000,
          60000000, 65000000, 70000000, 75000000, 85000000,
        ],
        borderColor: '#4caf50',
        backgroundColor: 'rgba(76, 175, 80, 0.1)',
        fill: true,
        tension: 0.4,
      },
      {
        label: 'Décès',
        data: [
          1000000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000,
          4500000, 5000000, 5500000, 6000000, 6500000,
        ],
        borderColor: '#f44336',
        backgroundColor: 'rgba(244, 67, 54, 0.1)',
        fill: true,
        tension: 0.4,
      },
    ],
  };

  areaChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        align: 'end',
      },
      tooltip: {
        mode: 'index',
        intersect: false,
      },
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
      },
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(0, 0, 0, 0.1)',
        },
        ticks: {
          callback: function (tickValue: string | number) {
            const value = Number(tickValue);
            if (!isNaN(value) && value >= 1000000) {
              return (value / 1000000).toFixed(0) + 'M';
            }
            return tickValue;
          },
        },
      },
    },
    interaction: {
      mode: 'nearest',
      axis: 'x',
      intersect: false,
    },
  };

  constructor() {}

  ngOnInit(): void {}
}
