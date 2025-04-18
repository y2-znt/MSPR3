import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';
import { CountryData, CovidDataService, CovidStats } from '../../../services/covid-data.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-countries',
  standalone: true,
  imports: [CommonModule, MatFormFieldModule, MatInputModule, MatTableModule, MatSortModule, MatPaginatorModule],
  templateUrl: './countries.component.html',
  styleUrl: './countries.component.scss',
})
export class CountriesComponent implements AfterViewInit, OnInit, OnDestroy {
  displayedColumns: string[] = ['country', 'totalCases', 'deaths', 'recovered', 'mortalityRate', 'recoveryRate'];
  dataSource: MatTableDataSource<CountryData> = new MatTableDataSource<CountryData>();
  private destroy$ = new Subject<void>();
  isLoading = true;

  // Statistiques COVID de démonstration (en cas où les données réelles ne sont pas disponibles)
  diseaseName: string = 'COVID-19';
  totalCases: number = 66;
  totalDeaths: number = 99;
  totalRecoveries: number = 29;
  mortalityRate: number = 5.24;
  recoveryRate: number = 46.88;
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private covidDataService: CovidDataService,
    private cdr: ChangeDetectorRef
  ) {}
  
  ngOnInit(): void {
    console.log('CountriesComponent: ngOnInit');
    
    // Données de démonstration pour le tableau (pour être sûr d'avoir quelque chose à afficher)
    const mockData: CountryData[] = [
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
    
    // Assurer que nous avons des données initiales
    this.dataSource.data = mockData;
    
    // S'abonner aux mises à jour des statistiques globales COVID
    this.covidDataService.covidStats$
      .pipe(takeUntil(this.destroy$))
      .subscribe(stats => {
        if (stats) {
          console.log('COVID stats reçues dans CountriesComponent', stats);
          this.updateCovidStats(stats);
        }
      });
    
    // S'abonner aux mises à jour des données par pays
    this.covidDataService.countriesData$
      .pipe(takeUntil(this.destroy$))
      .subscribe(data => {
        if (data && data.length > 0) {
          console.log(`Données de ${data.length} pays reçues dans CountriesComponent`);
          this.dataSource.data = data;
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    
    // Vérifier si les données sont déjà disponibles
    const currentStats = this.covidDataService.getCovidStats();
    if (currentStats) {
      console.log('Utilisation des statistiques COVID existantes');
      this.updateCovidStats(currentStats);
    }
    
    const countriesData = this.covidDataService.getCountriesData();
    if (countriesData && countriesData.length > 0) {
      console.log(`Utilisation des données existantes pour ${countriesData.length} pays`);
      this.dataSource.data = countriesData;
      this.isLoading = false;
    }
    
    // Forcer la détection des changements
    this.cdr.detectChanges();
  }

  ngAfterViewInit(): void {
    console.log('CountriesComponent: ngAfterViewInit');
    
    // Configurer le paginator et le sort dès que possible
    setTimeout(() => {
      console.log('Paginator:', this.paginator);
      console.log('Sort:', this.sort);
      
      if (this.paginator) {
        this.dataSource.paginator = this.paginator;
      }
      
      if (this.sort) {
        this.dataSource.sort = this.sort;
      }
      
      console.log('DataSource data length:', this.dataSource.data.length);
    }, 100);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  updateCovidStats(stats: CovidStats): void {
    this.diseaseName = stats.diseaseName;
    this.totalCases = stats.totalCases;
    this.totalDeaths = stats.totalDeaths;
    this.totalRecoveries = stats.totalRecoveries;
    this.mortalityRate = stats.mortalityRate;
    this.recoveryRate = stats.recoveryRate;
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }
}
