import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ChartData, ChartOptions } from 'chart.js';
import { PredictRequest, PredictService } from '../../services/predict.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';



@Component({
  selector: 'app-predict',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
  ],
  templateUrl: './predict.component.html',
  styleUrls: ['./predict.component.scss']
})

export class PredictComponent {
  predictForm!: FormGroup;
  areaChartData: ChartData<'doughnut'> = {
    labels: ['Probabilité de la classe prédite', 'Autres'],
    datasets: [{ data: [40, 60], backgroundColor: ['#36A2EB', '#FF6384'] }],
  };
  areaChartOptions: ChartOptions = {
    responsive: true,
    plugins: {
      legend: { display: true },
      title: { display: true, text: 'Confiance dans la prédiction' }
    }
  };
  chartReady = true;

  constructor(
    private fb: FormBuilder,
    private predictService: PredictService
  ) {}

  ngOnInit(): void {
    this.predictForm = this.fb.group({
      confirmed_case: [0],
      date: [new Date().toISOString().split('T')[0]],
      deaths: [0],
      recovered: [0],
      location: [''],
      region: [''],
      country: [''],
      continent: [''],
      population: [0],
      who_region: [''],
    });
  }

  // onSubmit(): void {
  //   const payload: PredictRequest = this.predictForm.value;
  //   this.predictService.predict(payload).subscribe((res) => {
  //     const prob = res.probability;
  //     this.areaChartData.datasets[0].data = [prob, 1 - prob];
  //     this.chartReady = true;
  //   });
  // }

  


}
