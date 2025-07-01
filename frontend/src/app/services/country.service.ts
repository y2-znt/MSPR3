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
    size: number = 25,
    sort: string = 'name,asc'
  ): Observable<Page<Country>> {
    let params = new HttpParams()
      .append('page', page.toString())
      .append('size', size.toString())
      .append('sort', sort);

    return this.http.get<Page<Country>>(`${this.apiUrl}countries`, { params });
  }

  getCountryById(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.apiUrl}countries/${id}`);
  }

  getCountriesStats(countryNames: string[]): Observable<any[]> {
    let params = new HttpParams();
    countryNames.forEach((name) => {
      params = params.append('countries', name);
    });

    return this.http.get<any[]>(
      `${this.apiUrl}disease-cases/aggregated-by-date`,
      { params }
    );
  }
}
