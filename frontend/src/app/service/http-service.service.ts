import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class HttpServiceService {
  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;

  constructor() {}

  //Countries
  getAllCountries(): Observable<any[]> {
    console.log('HttpServiceService called', this.url);
    return this.http.get<any[]>(this.url + 'countries');
  }

  getCoutryById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'countries/' + id);
  }

  //Disease
  getAllDiseases(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'diseases');
  }

  getDiseaseById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'diseases/' + id);
  }

  //disease cases
  getAllDiseaseCases(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'disease-cases');
  }

  getDiseaseCaseById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'disease-cases/' + id);
  }

  //location
  getAllLocations(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'locations');
  }

  getLocationById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'locations/' + id);
  }

  //region
  getAllRegions(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'regions');
  }
  getRegionById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'regions/' + id);
  }
}
