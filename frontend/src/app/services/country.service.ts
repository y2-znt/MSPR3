import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Country } from '../models/country.model';
import { Page } from '../models/pagination.model';

@Injectable({
  providedIn: 'root',
})
export class CountryService {
  private http: HttpClient = inject(HttpClient);
  private apiUrl: string = environment.apiUrl;

  constructor() {}

  getAllCountries(
    page: number = 0,
    size: number = 10
  ): Observable<Page<Country>> {
    return this.http.get<Page<Country>>(
      `${this.apiUrl}countries?page=${page}&size=${size}`
    );
  }

  getCountryById(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.apiUrl}countries/${id}`);
  }

  getCountriesStats(countryName: string): Observable<any[]> {
    // Créer les paramètres HTTP
    let params = new HttpParams().append('countries', countryName);

    return this.http.get<any[]>(
      `${this.apiUrl}disease-cases/aggregated-by-date`,
      { params }
    );
  }
}
