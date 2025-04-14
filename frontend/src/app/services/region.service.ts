import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Region } from '../models/region.model';

@Injectable({
  providedIn: 'root',
})
export class RegionService {
  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;

  constructor() {}

  getAllRegions(): Observable<Region[]> {
    return this.http.get<Region[]>(`${this.url}regions`);
  }

  getRegionById(id: number): Observable<Region> {
    return this.http.get<Region>(`${this.url}regions/${id}`);
  }
}
