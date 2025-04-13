import { Region } from './region.model';

export interface Country {
  id: number;
  name: string;
  continent: string;
  whoRegion: string;
  population: number;
  totalTests: number;
  regions: Region[];
}
