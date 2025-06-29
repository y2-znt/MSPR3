import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ChartData, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { PredictRequest } from '../../models/predict.model';
import { PredictService } from '../../services/predict.service';
import { TranslatePipe } from '../../pipes/translate.pipe';

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
    BaseChartDirective,
    TranslatePipe,
  ],
  templateUrl: './predict.component.html',
  styleUrls: ['./predict.component.scss'],
})
export class PredictComponent {
  predictForm!: FormGroup;
  areaChartData: ChartData<'doughnut'> = {
    labels: ['Probabilité de la classe prédite', 'Autres'],
    datasets: [{ data: [50, 50], backgroundColor: ['#36A2EB', '#FF6384'] }],
  };
  areaChartOptions: ChartOptions = {
    responsive: true,
    plugins: {
      legend: { display: true },
      title: { display: true, text: 'Confiance dans la prédiction' },
    },
  };
  chartReady = false;
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

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

  lastPrediction: number | null = null;

  onSubmit(): void {
    const payload: PredictRequest = this.predictForm.value;
    this.predictService.predict(payload).subscribe((res) => {
      const prob = res.probability;
      this.lastPrediction = res.prediction;
      this.areaChartData = {
        labels:
          this.lastPrediction === 1
            ? ['Contaminé', 'Pas contaminé']
            : ['Pas contaminé', 'Contaminé'],
        datasets: [
          {
            data:
              this.lastPrediction === 1 ? [prob, 1 - prob] : [1 - prob, prob],
            backgroundColor: ['#FF6384', '#36A2EB'],
          },
        ],
      };
      this.chartReady = true;
      this.chart?.update();
    });
  }
}
