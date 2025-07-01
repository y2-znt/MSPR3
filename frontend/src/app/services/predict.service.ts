import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { PredictRequest, PredictResponse } from '../models/predict.model';

@Injectable({
  providedIn: 'root',
})
export class PredictService {
  private apiAi: string = environment.apiAi;
  private headers = new HttpHeaders().set('Content-Type', 'application/json');

  constructor(private http: HttpClient) {}

  predict(data: PredictRequest): Observable<PredictResponse> {
    return this.http.post<PredictResponse>(this.apiAi, data, {
      headers: this.headers,
    });
  }
}
