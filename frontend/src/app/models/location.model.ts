import { DiseaseCase } from './diseaseCase.model';

export interface Location {
  id: number;
  name: string;
  diseasesCases: DiseaseCase[];
}
