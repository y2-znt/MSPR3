import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Country } from '../models/country.model';

@Injectable({
  providedIn: 'root',
})
export class CountryService {
  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;

  constructor() {}

  getAllCountries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${this.url}countries`);
  }

  getCountryById(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.url}countries/${id}`);
  }
}
