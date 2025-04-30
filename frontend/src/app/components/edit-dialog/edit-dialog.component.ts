import { Component, Inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogModule,
} from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DiseaseCaseService } from '../../services/disease-case.service';

@Component({
  selector: 'app-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatDatepickerModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './edit-dialog.component.html',
  styleUrl: './edit-dialog.component.scss',
})
export class EditDialogComponent implements OnInit {
  caseForm: FormGroup;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private diseaseCaseService: DiseaseCaseService,
    public dialogRef: MatDialogRef<EditDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      id: number | string;
      country: string;
      date: string;
      confirmedCases: number;
      deaths: number;
      recovered: number;
      countries: any[];
    }
  ) {
    this.caseForm = this.fb.group({
      country: ['', Validators.required],
      date: [new Date(), Validators.required],
      confirmedCases: [0, [Validators.required, Validators.min(0)]],
      deaths: [0, [Validators.required, Validators.min(0)]],
      recovered: [0, [Validators.required, Validators.min(0)]],
    });
  }

  ngOnInit(): void {
    const caseDate = this.data.date ? new Date(this.data.date) : new Date();

    this.caseForm.setValue({
      country: this.data.country,
      date: caseDate,
      confirmedCases: this.data.confirmedCases,
      deaths: this.data.deaths,
      recovered: this.data.recovered,
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.caseForm.valid) {
      this.isSubmitting = true;

      const formValue = this.caseForm.value;
      const formattedDate = this.formatDate(formValue.date);

      const payload = {
        country: formValue.country,
        date: formattedDate,
        confirmedCases: formValue.confirmedCases,
        deaths: formValue.deaths,
        recovered: formValue.recovered,
      };

      this.diseaseCaseService
        .updateDiseaseCase(this.data.id, payload)
        .subscribe({
          next: (response) => {
            this.dialogRef.close(response);
          },
          error: (error) => {
            console.error('Error updating case:', error);
            this.isSubmitting = false;
          },
        });
    }
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
