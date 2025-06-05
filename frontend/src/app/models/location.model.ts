import { Region } from './region.model';

export interface Location {
  id: number;
  name: string;
  region: Region;
}
