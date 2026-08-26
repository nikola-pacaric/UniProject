import { Component, OnInit, inject, signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Observable, catchError, EMPTY } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorResponse } from '../../../core/auth/auth.models';
import { Category } from '../category.model';
import { CategoryService } from '../category.service';

@Component({
    selector: 'app-category-list',
    imports: [
        AsyncPipe, 
        MatTableModule,
        MatIconModule,
        MatButtonModule,
        MatSnackBarModule,
        RouterLink,
    ],
    templateUrl: './category-list.component.html',
    styleUrl: './category-list.component.scss',
})
export class CategoryList implements OnInit {
    private readonly categoryService = inject(CategoryService);
    private readonly snackBar = inject(MatSnackBar);

    categories$!: Observable<Category[]>;

    readonly displayedColumns = [
        'name',
        'description',
        'actions',
    ];

    readonly errorMessage = signal<string | null>(null);
    readonly isDeleting = signal<number | null>(null);

    ngOnInit(): void {
        this.loadCategories();
    }

    deleteCategory(category: Category): void {
        const confirmed = window.confirm(`Da li zelite da obrisete kategoriju ${category.name}?`);

        if (!confirmed) {
            return;
        }

        this.isDeleting.set(category.id);

        this.categoryService.delete(category.id).subscribe({
            next: () => {
                this.isDeleting.set(null);
                this.snackBar.open('Kategorija je uspesno obrisana.', 'Zatvori', { duration: 3000 });
                this.loadCategories();
            },
            error: (error: HttpErrorResponse) => {
                this.isDeleting.set(null);

                const apiError = error.error as Partial<ApiErrorResponse> | null;

                this.errorMessage.set(apiError?.message ?? 'Brisanje kategorije nije uspelo.');
            }
        });
    }

    private loadCategories(): void {
        this.errorMessage.set(null);

        this.categories$ = this.categoryService.getAll().pipe(
            catchError((error: HttpErrorResponse) => {
                const apiError = error.error as Partial<ApiErrorResponse> | null;

                const message = error.status === 401 
                ? 'Sesija nije validna. Odjavite se i prijavite ponovo.' 
                : error.status === 0 
                ? 'Backend nije dostupan ili CORS nije podesen.' 
                : apiError?.message ?? 
                `Ucitavanje kategorija nije uspelo. Status: ${error.status}`;
                
                this.errorMessage.set(message);

                return EMPTY;
            }),
        );
    }
}