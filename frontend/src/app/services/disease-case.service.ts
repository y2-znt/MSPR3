import { HttpClient, HttpParams } from '@angular/common/http';
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

  getAllDiseaseCases(page: number = 0, size: number = 250): Observable<any> {
    return this.http.get(`${this.url}disease-cases?page=${page}&size=${size}`);
  }

  getDiseaseCaseById(id: number): Observable<DiseaseCase> {
    return this.http.get<DiseaseCase>(`${this.url}disease-cases/${id}`);
  }

  getDiseaseCases(page: number, size: number): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<any>(this.url, { params });
  }
}
