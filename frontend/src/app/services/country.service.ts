import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Country } from '../models/country.model';
import { Page } from '../models/pagination.model';

@Injectable({
  providedIn: 'root',
})
export class CountryService {
  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;

  constructor() {}

  getAllCountries(
    page: number = 0,
    size: number = 10
  ): Observable<Page<Country>> {
    return this.http.get<Page<Country>>(
      `${this.url}countries?page=${page}&size=${size}`
    );
  }
  

  getCountryById(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.url}countries/${id}`);
  }
}
