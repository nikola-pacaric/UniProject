import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators, } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorResponse, RegisterRequest, } from '../../../core/auth/auth.models';
import { AuthService } from '../../../core/auth/auth.service';

function passwordMatchValidator(form: AbstractControl, ): ValidationErrors | null {
  const password = form.get('password')?.value;
  const confirmPassword = form.get('confirmPassword')?.value;

  return password === confirmPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class Register {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService)
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly registerForm = this.formBuilder.nonNullable.group(
    {
      fullName: ['', Validators.maxLength(150)],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordMatchValidator, },
  );

  submit(): void {
    this.normalizeTextFields();

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const formValue = this.registerForm.getRawValue();

    const request: RegisterRequest = {
      username: formValue.username,
      email: formValue.email,
      password: formValue.password,
      fullName: formValue.fullName || null,
    };

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService.register(request).subscribe({
      next: () => {
        this.isSubmitting.set(false);

        this.snackBar.open('Registracija je uspesna. Sada se prijavite.', 'Zatvori', { duration: 5000, });

        void this.router.navigate(['/login']);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.getErrorMessage(error));
      },
    });
  }

  private normalizeTextFields(): void {
    this.registerForm.patchValue({
      fullName: this.registerForm.controls.fullName.value.trim(),
      username: this.registerForm.controls.username.value.trim(),
      email: this.registerForm.controls.email.value.trim(),
    });
  }

  private getErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Backend nije dostupan. Proverite da li je server pokrenut.';
    }

    const apiError = error.error as Partial<ApiErrorResponse> | null;

    if (apiError?.message === 'Username already taken') {
      return 'Korisnicko ime je vec zauzeto.';
    }

    if (apiError?.message === 'Email already registered') {
      return 'Email adresa je vec registrovana.';
    }

    const firstFieldError = apiError?.fieldErrors ? Object.values(apiError.fieldErrors)[0] : null;

    return (firstFieldError ?? apiError?.message ?? 'Registracija nije uspela.');
  }
}
