export interface DiseaseCase {
  id: number;
  disease: string;
  date: string;
  confirmedCases: number;
  deaths: number;
  recovered: number;
  location: Location[];
}
