export interface PredictRequest {
  confirmed_case: number;
  date: string;
  deaths: number;
  recovered: number;
  location: string;
  region: string;
  country: string;
  continent: string;
  population: number;
  who_region: string;
}

export interface PredictResponse {
  prediction: number;
  probability: number;
  features_length: number;
}
