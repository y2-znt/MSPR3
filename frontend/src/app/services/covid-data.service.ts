import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface CovidStats {
  diseaseName: string;
  totalCases: number;
  totalDeaths: number;
  totalRecoveries: number;
  mortalityRate: number;
  recoveryRate: number;
}

export interface CountryData {
  country: string;
  totalCases: number;
  deaths: number;
  recovered: number;
  mortalityRate: number;
  recoveryRate: number;
}

@Injectable({
  providedIn: 'root'
})
export class CovidDataService {
  private covidStatsSubject = new BehaviorSubject<CovidStats | null>(null);
  covidStats$ = this.covidStatsSubject.asObservable();
  
  private countriesDataSubject = new BehaviorSubject<CountryData[]>([]);
  countriesData$ = this.countriesDataSubject.asObservable();

  constructor() { }

  updateCovidStats(stats: CovidStats): void {
    this.covidStatsSubject.next(stats);
  }

  getCovidStats(): CovidStats | null {
    return this.covidStatsSubject.value;
  }
  
  updateCountriesData(data: CountryData[]): void {
    this.countriesDataSubject.next(data);
  }
  
  getCountriesData(): CountryData[] {
    return this.countriesDataSubject.value;
  }
}