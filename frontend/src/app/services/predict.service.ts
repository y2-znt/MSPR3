import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment.development';
const apiAi = environment.apiAi;


export interface PredictRequest {
  confirmed_case: number;
  date: string;
  deaths: number;
  recovered: number;
  location: string;
  region: string;
  country: string;
  continent: string;
  population: number;
  who_region: string;
}

export interface PredictResponse {
  prediction: number;
  probability: number;
  features_length: number;
}

@Injectable({
  providedIn: 'root'
})
export class PredictService {

  constructor(private http: HttpClient) { }

  predict(data: PredictRequest): Observable<PredictResponse> {
    return this.http.post<PredictResponse>(`${apiAi}`, data);
  }
}
