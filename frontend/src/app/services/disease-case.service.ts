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
import { map, tap } from 'rxjs/operators';

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

  /**
   * Get aggregated cases by date with optional filtering
   */
  getAggregatedCasesByDate(startDate?: string, endDate?: string): Observable<AggregatedDiseaseCase[]> {
    let params = new HttpParams();
    
    if (startDate) {
      params = params.set('start', startDate);
    }
    
    if (endDate) {
      params = params.set('end', endDate);
    }
    
    return this.http.get<AggregatedDiseaseCase[]>(
      `${this.url}disease-cases/aggregated-by-date`,
      { params }
    );
  }

  /**
   * Get aggregated cases by date for specific countries with optional date filtering
   */
  getAggregatedCasesByDateAndCountries(
    countries: Country[], 
    startDate?: string, 
    endDate?: string
  ): Observable<AggregatedDiseaseCase[]> {
    if (!countries || countries.length === 0) {
      return this.getAggregatedCasesByDate(startDate, endDate);
    }
    
    // Extraire les noms des pays
    const countryNames = countries.map(country => country.name).join(',');
    console.log(`Fetching data for countries: ${countryNames}, period: ${startDate || 'all'} to ${endDate || 'now'}`);
    
    let params = new HttpParams().set('countries', countryNames);
    
    if (startDate) {
      params = params.set('start', startDate);
    }
    
    if (endDate) {
      params = params.set('end', endDate);
    }
    
    return this.http.get<any[]>(`${this.url}disease-cases/aggregated-by-date`, { params })
      .pipe(
        tap(response => console.log('API response for filtered data:', response)),
        map(data => data as AggregatedDiseaseCase[])
      );
  }

  /**
   * Met à jour un cas COVID existant par son ID
   */
  updateDiseaseCase(
    id: number | string,
    updateData: {
      country: string,
      date: string,
      confirmedCases: number,
      deaths: number,
      recovered: number
    }
  ): Observable<any> {
    // Si l'ID est de type chaîne et commence par "temp-", c'est un ID temporaire
    // Dans ce cas, nous devons créer un nouveau cas au lieu de mettre à jour
    if (typeof id === 'string' && id.startsWith('temp-')) {
      console.log('ID temporaire détecté, création d\'un nouveau cas au lieu de mise à jour');
      return this.http.post(
        `${this.url}disease-cases/aggregated-by-date`,
        updateData
      ).pipe(
        tap(response => console.log('Create response:', response))
      );
    }
    
    // Sinon, procéder à la mise à jour normalement
    return this.http.put(
      `${this.url}disease-cases/aggregated-by-date/${id}`,
      updateData
    ).pipe(
      tap(response => console.log('Update response:', response))
    );
  }
}
