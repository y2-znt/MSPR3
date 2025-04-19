export interface DiseaseCase {
  id: number;
  disease: string;
  date: string;
  confirmedCases: number;
  deaths: number;
  recovered: number;
  location: Location[];
}

export interface TotalKpiDto {
  totalCases: number;
  totalDeaths: number;
  totalRecovered: number;
  mortalityRate: number;
  recoveryRate: number;
}

export interface AggregatedDiseaseCase {
  date: string;
  recovered: number;
  confirmedCases: number;
  deaths: number;
}
