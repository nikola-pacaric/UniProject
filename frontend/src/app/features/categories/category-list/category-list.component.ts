import { Component, OnInit, inject, signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Observable, catchError, EMPTY } from 'rxjs';
import { ConfirmationDialogService } from '../../../shared/confirmation-dialog/confirmation-dialog.service';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
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
    private readonly confirmationDialog = inject(ConfirmationDialogService);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

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
        this.confirmationDialog.confirm({
            title: 'Brisanje kategorije',
            message: `Da li želite da obrišete kategoriju ${category.name}?`,
            confirmText: 'Obriši',
        }).subscribe((confirmed) => {
            if (!confirmed) {
                return;
            }
            this.performDelete(category);
        });
    }

    private performDelete(category: Category): void {
        this.errorMessage.set(null);
        this.isDeleting.set(category.id);

        this.categoryService.delete(category.id).subscribe({
            next: () => {
                this.isDeleting.set(null);
                this.snackBar.open(
                    'Kategorija je uspešno obrisana.',
                    'Zatvori',
                    { duration: 3000 },
                );
                this.loadCategories();
            },
            error: (error: HttpErrorResponse) => {
                this.isDeleting.set(null);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Brisanje kategorije nije uspelo.',
                    ),
                );
            },
        });
    }

    private loadCategories(): void {
        this.errorMessage.set(null);

        this.categories$ = this.categoryService.getAll().pipe(
            catchError((error: HttpErrorResponse) => {
                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje kategorija nije uspelo.',
                    ),
                );
                return EMPTY;
            }),
        );
    }
}