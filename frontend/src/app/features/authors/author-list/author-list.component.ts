import { Component, OnInit, inject, signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Observable, catchError, EMPTY } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ConfirmationDialogService } from '../../../shared/confirmation-dialog/confirmation-dialog.service';
import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';

import { Author } from '../author.model';
import { AuthorService } from '../author.service';

@Component({
    selector: 'app-author-list',
    imports: [
        AsyncPipe, 
        MatTableModule,
        MatIconModule,
        MatButtonModule,
        MatSnackBarModule,
        RouterLink,
    ],
    templateUrl: './author-list.component.html',
    styleUrl: './author-list.component.scss',
})
export class AuthorList implements OnInit {
    private readonly authorService = inject(AuthorService);
    private readonly snackBar = inject(MatSnackBar);
    private readonly confirmationDialog = inject(ConfirmationDialogService);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    authors$!: Observable<Author[]>;

    readonly displayedColumns = [
        'name',
        'biography',
        'actions',
    ];

    readonly errorMessage = signal<string | null>(null);
    readonly isDeleting = signal<number | null>(null);

    ngOnInit(): void {
        this.loadAuthors();
    }

    deleteAuthor(author: Author): void {
        this.confirmationDialog.confirm({
            title: 'Brisanje autora',
            message: `Da li želite da obrišete autora ${author.firstName} ${author.lastName}?`,
            confirmText: 'Obriši',
        }).subscribe((confirmed) => {
            if (!confirmed) {
                return;
            }

            this.performDelete(author);
        });
    }

    private performDelete(author: Author): void {
        this.errorMessage.set(null);
        this.isDeleting.set(author.id);

        this.authorService.delete(author.id).subscribe({
            next: () => {
                this.isDeleting.set(null);
                this.snackBar.open(
                    'Autor je uspešno obrisan.',
                    'Zatvori',
                    { duration: 3000 },
                );
                this.loadAuthors();
            },
            error: (error: HttpErrorResponse) => {
                this.isDeleting.set(null);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Brisanje autora nije uspelo.',
                    ),
                );
            },
        });
    }

    private loadAuthors(): void {
        this.errorMessage.set(null);

        this.authors$ = this.authorService.getAll().pipe(
            catchError((error: HttpErrorResponse) => {
                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje autora nije uspelo.',
                    )
                );
                return EMPTY;
            }),
        );
    }
}