import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import {
  AggregatedDiseaseCase,
  DiseaseCase,
  TotalKpiDto,
} from '../models/diseaseCase.model';
import { Country } from '../models/country.model';

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

  getTotalKpi(): Observable<TotalKpiDto> {
    return this.http.get<TotalKpiDto>(`${this.url}disease-cases/kpi`);
  }

  getAggregatedCasesByDate(): Observable<AggregatedDiseaseCase[]> {
    return this.http.get<AggregatedDiseaseCase[]>(
      `${this.url}disease-cases/aggregated-by-date`
    );
  }

  getAggregatedCasesByDateAndCountries(countries: Country[]): Observable<AggregatedDiseaseCase[]> {
    if (!countries || countries.length === 0) {
      return this.getAggregatedCasesByDate();
    }
    
    // Extraire les noms des pays
    const countryNames = countries.map(country => country.name).join(',');
    console.log('Fetching data for countries:', countryNames);
    
    const params = new HttpParams().set('countries', countryNames);
    
    return this.http.get<any[]>(`${this.url}disease-cases/aggregated-by-date`, { params });
  }
}
