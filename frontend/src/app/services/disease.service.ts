import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.development';
import { Disease } from '../models/disease.model';

@Injectable({
  providedIn: 'root',
})
export class DiseaseService {
  private http: HttpClient = inject(HttpClient);
  private apiUrl: string = environment.apiUrl;

  constructor() {}

  getAllDiseases(): Observable<Disease[]> {
    return this.http.get<Disease[]>(`${this.apiUrl}diseases`);
  }

  getDiseaseById(id: number): Observable<Disease> {
    return this.http.get<Disease>(`${this.apiUrl}diseases/${id}`);
  }
}
