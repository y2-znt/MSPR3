import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { DiseaseCase } from '../models/diseaseCase.model';

@Injectable({
  providedIn: 'root',
})
export class DiseaseCaseService {
  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;

  constructor() {}

  getAllDiseaseCases(): Observable<DiseaseCase[]> {
    return this.http.get<DiseaseCase[]>(`${this.url}disease-cases`);
  }

  getDiseaseCaseById(id: number): Observable<DiseaseCase> {
    return this.http.get<DiseaseCase>(`${this.url}disease-cases/${id}`);
  }
}
