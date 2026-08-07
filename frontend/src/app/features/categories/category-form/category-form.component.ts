import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorResponse } from '../../../core/auth/auth.models';
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
            error: () => {
                this.errorMessage.set('Ucitavanje kategorije nije uspelo.');
                this.isLoading.set(false);
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
                    ? 'Kategorija je uspesno izmenjena.' 
                    : 'Kategorija je uspesno kreirana.', 'Zatvori', { duration: 3000, }
                );
                void this.router.navigate(['/categories']);
            },
            error: (error: HttpErrorResponse) => {
                this.isSubmitting.set(false);

                const apiError = error.error as Partial<ApiErrorResponse> | null;
                this.errorMessage.set(apiError?.message ?? 'Doslo je do greske prilikom cuvanja kategorije.');
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