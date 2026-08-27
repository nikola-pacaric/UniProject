import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { AuthorRequest } from '../author.model';
import { AuthorService } from '../author.service';

@Component({
    selector: 'app-author-form',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule,
    ],
    templateUrl: './author-form.component.html',
    styleUrl: './author-form.component.scss',
})
export class AuthorForm implements OnInit {
    private readonly formBuilder = inject(FormBuilder);
    private readonly authorService = inject(AuthorService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly snackBar = inject(MatSnackBar);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    readonly isLoading = signal(false);
    readonly isSubmitting = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly authorForm = this.formBuilder.nonNullable.group({
        firstName: ['', [Validators.required, Validators.maxLength(100)]],
        lastName: ['', [Validators.required, Validators.maxLength(100)]],
        biography: ['', Validators.maxLength(5000)],
    });

    get authorId(): number | null {
        const value = this.route.snapshot.paramMap.get('id');
        return value ? Number(value) : null;
    }

    get isEditMode(): boolean {
        return this.authorId !== null;
    }

    ngOnInit(): void {
        if (this.authorId === null) {
            return;
        }

        this.isLoading.set(true);

        this.authorService.getById(this.authorId).subscribe({
            next: (author) => {
                this.authorForm.patchValue({
                    firstName: author.firstName,
                    lastName: author.lastName,
                    biography: author.biography ?? '',
                });
                this.isLoading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje autora nije uspelo.',
                    ),
                );
            },
        });
    }

    submit(): void {
        this.normalizeFields();

        if (this.authorForm.invalid) {
            this.authorForm.markAllAsTouched();
            return;
        }

        const values = this.authorForm.getRawValue();

        const request: AuthorRequest = {
            firstName: values.firstName,
            lastName: values.lastName,
            biography: values.biography || null,
        };

        this.isSubmitting.set(true);
        this.errorMessage.set(null);

        const operation = this.authorId === null ? this.authorService.create(request) : this.authorService.update(this.authorId, request);

        operation.subscribe({
            next: () => {
                this.snackBar.open(
                    this.isEditMode 
                    ? 'Autor je uspešno izmenjen.' 
                    : 'Autor je uspešno kreiran.', 
                    'Zatvori', 
                    { duration: 3000 },
                );
            
                void this.router.navigate(['/authors']);  
            },
            error: (error: HttpErrorResponse) => {
                this.isSubmitting.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Čuvanje autora nije uspelo.',
                    ),
                );
            },
        });
    }

    private normalizeFields(): void {
        this.authorForm.patchValue({
            firstName: this.authorForm.controls.firstName.value.trim(),
            lastName: this.authorForm.controls.lastName.value.trim(),
            biography: this.authorForm.controls.biography.value.trim(),
        });
    }
}
