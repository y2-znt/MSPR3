import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment.development';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HttpServiceService {

  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;
  
  constructor() {}


  //Countries
  getAllCountries(): Observable<any[]> {
    console.log('HttpServiceService called',this.url);
    return this.http.get<any[]>(this.url + 'country');
  }
  
  getCoutryById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'country/' + id);
  }

  //Disease
  getAllDiseases(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'disease');
  }

  getDiseaseById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'disease/' + id);
  }

  //disease cases
  getAllDiseaseCases(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'disease-case');
  }

  getDiseaseCaseById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'disease-case/' + id);
  }

  //location
  getAllLocations(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'location');
  }

  getLocationById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'location/' + id);
  }

  //region
  getAllRegions(): Observable<any[]> {
    return this.http.get<any[]>(this.url + 'region');
  }
  getRegionById(id: number): Observable<any> {
    return this.http.get<any>(this.url + 'region/' + id);
  }

}
