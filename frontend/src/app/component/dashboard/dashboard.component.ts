import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { provideNativeDateAdapter } from '@angular/material/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { ChartConfiguration } from 'chart.js';
import { ChartOptions, ChartType, ChartData,  } from 'chart.js';
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
    BaseChartDirective,
    
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  myControl = new FormControl('');
  options: string[] = ['One', 'Two', 'Three'];

  lineChartData: ChartData<'line'> = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr'],
    datasets: [
      {
        label: 'Sales',
        data: [100, 150, 125, 200],
        borderColor: 'rgba(75,192,192,1)',
        fill: false
      }
    ]
  };
  
  lineChartOptions: ChartOptions = {
    responsive: true
  };

  // doughnutChartData: ChartData<'doughnut'> = {
  //   labels: ['Red', 'Blue', 'Yellow'],
  //   datasets: [
  //     {
  //       data: [300, 500, 100],
  //       backgroundColor: ['#ff6384', '#36a2eb', '#ffce56']
  //     }
  //   ]
  // };
  
  // doughnutChartOptions: ChartOptions = {
  //   responsive: true,
  //   plugins: {
  //     legend: {
  //       position: 'top',
  //     },
  //   },
  // };
  // Doughnut
  public doughnutChartLabels: string[] = [ 'Download Sales', 'In-Store Sales', 'Mail-Order Sales' ];
  public doughnutChartDatasets: ChartConfiguration<'doughnut'>['data']['datasets'] = [
      { data: [ 350, 450, 100 ], label: 'Series A' },
      { data: [ 50, 150, 120 ], label: 'Series B' },
      { data: [ 250, 130, 70 ], label: 'Series C' }
    ];

  public doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: false
  };

  // polarChartData: ChartData<'polarArea'> = {
  //   labels: ['A', 'B', 'C', 'D'],
  //   datasets: [
  //     {
  //       data: [11, 16, 7, 3],
  //       backgroundColor: ['#FF6384', '#4BC0C0', '#FFCE56', '#E7E9ED']
  //     }
  //   ]
  // };
  
  // polarChartOptions: ChartOptions = {
  //   responsive: true,
  //   plugins: {
  //     legend: {
  //       position: 'top',
  //     },
  //   },
  // };
    // PolarArea
    public polarAreaChartLabels: string[] = [ 'Download Sales', 'In-Store Sales', 'Mail Sales', 'Telesales', 'Corporate Sales' ];
    public polarAreaChartDatasets: ChartConfiguration<'polarArea'>['data']['datasets'] = [
      { data: [ 300, 500, 100, 40, 120 ] }
    ];
    public polarAreaLegend = true;
  
    public polarAreaOptions: ChartConfiguration<'polarArea'>['options'] = {
      responsive: false,
    };

}
