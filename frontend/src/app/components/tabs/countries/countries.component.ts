import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {DiseaseCaseService} from '../../../services/disease-case.service';
import {CommonModule} from '@angular/common';
import { CovidDataService, CovidStats } from '../../../services/covid-data.service';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';

interface CountryData {
  country: string;
  totalCases: number;
  deaths: number;
  recovered: number;
  mortalityRate: number;
  recoveryRate: number;
}

@Component({
  selector: 'app-countries',
  standalone: true,
  imports: [CommonModule, MatFormFieldModule, MatInputModule, MatTableModule, MatSortModule, MatPaginatorModule],
  templateUrl: './countries.component.html',
  styleUrl: './countries.component.scss',
})
export class CountriesComponent implements AfterViewInit, OnInit {
  displayedColumns: string[] = ['country', 'totalCases', 'deaths', 'recovered', 'mortalityRate', 'recoveryRate'];
  dataSource: MatTableDataSource<CountryData> = new MatTableDataSource();
  private destroy$ = new Subject<void>();

  // COVID statistics
  totalCases: number = 0;
  totalDeaths: number = 0;
  totalRecoveries: number = 0;
  mortalityRate: number = 0;
  recoveryRate: number = 0;
  diseaseName: string = '';
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private diseaseCaseService: DiseaseCaseService,
    private covidDataService: CovidDataService
  ) {}
  
  ngOnInit(): void {
    // Subscribe to COVID data updates
    this.covidDataService.covidStats$
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        if (stats) {
          console.log('COVID stats received', stats);
          this.updateCovidStats(stats);
          this.getCountryData();
        } else {
          console.log('No COVID stats available, fetching data...');
          this.getAllDiseasesCases(); // Fallback if data isn't already loaded
        }
      });
    
    // Check if the data is already available
    const currentStats = this.covidDataService.getCovidStats();
    if (currentStats) {
      console.log('Using existing COVID stats');
      this.updateCovidStats(currentStats);
      this.getCountryData();
    }
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  updateCovidStats(stats: CovidStats) {
    this.diseaseName = stats.diseaseName;
    this.totalCases = stats.totalCases;
    this.totalDeaths = stats.totalDeaths;
    this.totalRecoveries = stats.totalRecoveries;
    this.mortalityRate = stats.mortalityRate;
    this.recoveryRate = stats.recoveryRate;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  getCountryData() {
    // We'll just populate the table with some sample data for now
    // In a real app, you would use the DiseaseCaseService to get country-specific data
    this.dataSource.data = [
      {
        country: 'United States',
        totalCases: 103594491,
        deaths: 1127152,
        recovered: 100619424,
        mortalityRate: 1.09,
        recoveryRate: 97.13
      },
      {
        country: 'India',
        totalCases: 44986461,
        deaths: 531832,
        recovered: 44446514,
        mortalityRate: 1.18,
        recoveryRate: 98.8
      },
      {
        country: 'France',
        totalCases: 39908640,
        deaths: 165624,
        recovered: 39723212,
        mortalityRate: 0.41,
        recoveryRate: 99.54
      },
      {
        country: 'Germany',
        totalCases: 38431588,
        deaths: 173585,
        recovered: 38240100,
        mortalityRate: 0.45,
        recoveryRate: 99.5
      },
      {
        country: 'Brazil',
        totalCases: 37608203,
        deaths: 704394,
        recovered: 36895086,
        mortalityRate: 1.87,
        recoveryRate: 98.1
      }
    ];
  }

  public getAllDiseasesCases(): void {
    const allCases: any[] = [];
    let page = 0;
    const pageSize = 250;

    const fetchPage = () => {
      console.log(`📄 Récupération de la page ${page}`);

      this.diseaseCaseService
        .getAllDiseaseCases(page, pageSize)
        .subscribe({
          next: (res: any) => {
            console.log(`📥 Page ${page} reçue`, res);

            if (!res.content || !Array.isArray(res.content)) {
              console.warn(
                '⚠️ Structure inattendue, pas de "content" dans la réponse.'
              );
              return;
            }

            allCases.push(...res.content);
            
            if (!res.last) {
              page++;
              fetchPage();
            } else {
              console.log('✅ Tous les cas récupérés:', allCases);

              this.diseaseName = allCases[0]?.name || 'COVID-19';
              
              // Group cases by country
              const countryMap = new Map<string, CountryData>();
              
              allCases.forEach(item => {
                const countryName = item.country?.name || 'Unknown';
                
                if (!countryMap.has(countryName)) {
                  countryMap.set(countryName, {
                    country: countryName,
                    totalCases: 0,
                    deaths: 0,
                    recovered: 0,
                    mortalityRate: 0,
                    recoveryRate: 0
                  });
                }
                
                const countryData = countryMap.get(countryName)!;
                countryData.totalCases += item.confirmedCases || 0;
                countryData.deaths += item.deaths || 0;
                countryData.recovered += item.recovered || 0;
              });
              
              // Calculate rates for each country
              countryMap.forEach(data => {
                data.mortalityRate = data.totalCases > 0 ? +(data.deaths / data.totalCases * 100).toFixed(2) : 0;
                data.recoveryRate = data.totalCases > 0 ? +(data.recovered / data.totalCases * 100).toFixed(2) : 0;
              });
              
              // Update datasource
              this.dataSource.data = Array.from(countryMap.values());
              
              // Calculate global statistics
              this.totalCases = allCases.reduce(
                (sum, item) => sum + (item.confirmedCases || 0),
                0
              );
              this.totalDeaths = allCases.reduce(
                (sum, item) => sum + (item.deaths || 0),
                0
              );
              this.totalRecoveries = allCases.reduce(
                (sum, item) => sum + (item.recovered || 0),
                0
              );

              this.mortalityRate = this.totalCases
                ? +((this.totalDeaths / this.totalCases) * 100).toFixed(2)
                : 0;
              this.recoveryRate = this.totalCases
                ? +((this.totalRecoveries / this.totalCases) * 100).toFixed(2)
                : 0;

              // Save stats to the service
              this.covidDataService.updateCovidStats({
                diseaseName: this.diseaseName,
                totalCases: this.totalCases,
                totalDeaths: this.totalDeaths,
                totalRecoveries: this.totalRecoveries,
                mortalityRate: this.mortalityRate,
                recoveryRate: this.recoveryRate
              });
            }
          },
          error: (err) => {
            console.error(
              '❌ Erreur lors de la récupération des données:',
              err
            );
          },
        });
    };

    fetchPage();
  }
}
