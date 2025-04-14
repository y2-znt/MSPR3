import { Location } from './location.model';

export interface Region {
  id: number;
  name: string;
  locations: Location[];
}
