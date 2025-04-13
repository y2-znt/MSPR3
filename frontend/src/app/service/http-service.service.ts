import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Country } from '../models/country.model';
import { Disease } from '../models/disease.model';
import { DiseaseCase } from '../models/diseaseCase.model';
import { Location } from '../models/location.model';
import { Region } from '../models/region.model';

@Injectable({
  providedIn: 'root',
})
export class HttpServiceService {
  private http: HttpClient = inject(HttpClient);
  private url: string = environment.apiUrl;

  constructor() {}

  // Countries
  getAllCountries(): Observable<Country[]> {
    console.log('HttpServiceService called', this.url);
    return this.http.get<Country[]>(this.url + 'countries');
  }

  getCountryById(id: number): Observable<Country> {
    return this.http.get<Country>(this.url + 'countries/' + id);
  }

  //Diseases
  getAllDiseases(): Observable<Disease[]> {
    return this.http.get<Disease[]>(this.url + 'diseases');
  }

  getDiseaseById(id: number): Observable<Disease> {
    return this.http.get<Disease>(this.url + 'diseases/' + id);
  }

  //Disease Cases
  getAllDiseaseCases(): Observable<DiseaseCase[]> {
    return this.http.get<DiseaseCase[]>(this.url + 'disease-cases');
  }

  getDiseaseCaseById(id: number): Observable<DiseaseCase> {
    return this.http.get<DiseaseCase>(this.url + 'disease-cases/' + id);
  }

  //Locations
  getAllLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(this.url + 'locations');
  }

  getLocationById(id: number): Observable<Location> {
    return this.http.get<Location>(this.url + 'locations/' + id);
  }

  //Regions
  getAllRegions(): Observable<Region[]> {
    return this.http.get<Region[]>(this.url + 'regions');
  }

  getRegionById(id: number): Observable<Region> {
    return this.http.get<Region>(this.url + 'regions/' + id);
  }
}
