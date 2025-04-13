import { DiseaseCase } from './diseaseCase.model';

export interface Disease {
  id: number;
  name: string;
  description: string;
  diseaseCases: DiseaseCase[];
}
