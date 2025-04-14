import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Location } from '../models/location.model';

@Injectable({
  providedIn: 'root',
})
export class LocationService {
  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;

  constructor() {}

  getAllLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(`${this.url}locations`);
  }

  getLocationById(id: number): Observable<Location> {
    return this.http.get<Location>(`${this.url}locations/${id}`);
  }
}
