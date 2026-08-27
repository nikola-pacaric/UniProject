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
import { CategoryRequest } from '../category.model';
import { CategoryService } from '../category.service';

@Component({
    selector: 'app-category-form',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule,
    ],
    templateUrl: './category-form.component.html',
    styleUrl: './category-form.component.scss',
})  
export class CategoryForm implements OnInit {
    private readonly formBuilder = inject(FormBuilder);
    private readonly categoryService = inject(CategoryService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly snackBar = inject(MatSnackBar);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    readonly isLoading = signal(false);
    readonly isSubmitting = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly categoryForm = this.formBuilder.nonNullable.group({
        name: ['', [Validators.required, Validators.maxLength(100)]],
        description: ['', Validators.maxLength(5000)],
    });

    get categoryId(): number | null {
        const value = this.route.snapshot.paramMap.get('id');
        return value ? Number(value) : null;
    }

    get isEditMode(): boolean {
        return this.categoryId !== null;
    }

    ngOnInit(): void {
        if (this.categoryId === null) {
            return;
        }

        this.isLoading.set(true);

        this.categoryService.getById(this.categoryId).subscribe({
            next: (category) => {
                this.categoryForm.patchValue({
                    name: category.name,
                    description: category.description ?? '',
                });
                this.isLoading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje kategorije nije uspelo.',
                    ),
                );
            },
        });
    }

    submit(): void {
        this.normalizeFields();

        if (this.categoryForm.invalid) {
            this.categoryForm.markAllAsTouched();
            return;
        }

        const values = this.categoryForm.getRawValue();

        const request: CategoryRequest = {
            name: values.name.trim(),
            description: values.description?.trim() || null,
        };

        this.isSubmitting.set(true);
        this.errorMessage.set(null);

        const operation = this.categoryId === null 
            ? this.categoryService.create(request)
            : this.categoryService.update(this.categoryId, request);

        operation.subscribe({
            next: () => {
                this.snackBar.open(this.isEditMode 
                    ? 'Kategorija je uspešno izmenjena.' 
                    : 'Kategorija je uspešno kreirana.', 'Zatvori', { duration: 3000, }
                );
                void this.router.navigate(['/categories']);
            },
            error: (error: HttpErrorResponse) => {
                this.isSubmitting.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Čuvanje kategorije nije uspelo.',
                    ),
                );
            },
        });
    }

    private normalizeFields(): void {
        this.categoryForm.patchValue({
            name: this.categoryForm.controls.name.value.trim(),
            description: this.categoryForm.controls.description.value.trim(),
        });
    }
} 