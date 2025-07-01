import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { PredictComponent } from './components/predict/predict.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'prediction', component: PredictComponent },

  { path: '**', redirectTo: '' },
];
