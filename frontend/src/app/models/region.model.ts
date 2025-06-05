import { Country } from './country.model';

export interface Region {
  id: number;
  name: string;
  country: Country;
}
