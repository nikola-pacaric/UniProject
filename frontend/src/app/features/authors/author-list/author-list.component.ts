import { Component, OnInit, inject, signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Observable, catchError, of, EMPTY } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorResponse } from '../../../core/auth/auth.models';
import { Author } from '../author.model';
import { AuthorService } from '../author.service';

@Component({
    selector: 'app-author-list',
    imports: [
        AsyncPipe, 
        MatListModule,
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

    authors$!: Observable<Author[]>;
    readonly errorMessage = signal<string | null>(null);
    readonly isDeleting = signal<number | null>(null);

    ngOnInit(): void {
        this.loadAuthors();
    }

    deleteAuthor(author: Author): void {
        const confirmed = window.confirm('Da li zelite da obrisete autora ${author.firstName} ${author.lastName}?');

        if (!confirmed) {
            return;
        }

        this.isDeleting.set(author.id);

        this.authorService.delete(author.id).subscribe({
            next: () => {
                this.isDeleting.set(null);
                this.snackBar.open('Autor je uspesno obrisan.', 'Zatvori', { duration: 3000 });
                this.loadAuthors();
            },
            error: (error: HttpErrorResponse) => {
                this.isDeleting.set(null);

                const apiError = error.error as Partial<ApiErrorResponse> | null;

                this.errorMessage.set(apiError?.message ?? 'Brisanje autora nije uspelo.');
            }
        });
    }

    private loadAuthors(): void {
        this.errorMessage.set(null);

        this.authors$ = this.authorService.getAll().pipe(
            catchError((error: HttpErrorResponse) => {
                const message = error.status === 401 ? 'Sesija nije validna. Odjavite se i prijavite ponovo.' : error.status === 0 ? 'Backend nije dostupan ili CORS nije podesen.' : 'Ucitavanje autora nije uspelo. Status: ${error.status}';
                
                this.errorMessage.set(message);

                return EMPTY;
            }),
        );
    }
}