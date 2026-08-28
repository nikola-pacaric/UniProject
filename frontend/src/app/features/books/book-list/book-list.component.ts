import { Component, inject, OnInit, signal } from '@angular/core';
import { Book } from '../book.model';
import { AsyncPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BookService } from '../book.service';
import { AuthorService } from '../../authors/author.service';
import { CategoryService } from '../../categories/category.service';
import { Author } from '../../authors/author.model';
import { Category } from '../../categories/category.model';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { ConfirmationDialogService } from '../../../shared/confirmation-dialog/confirmation-dialog.service';

import {
    EMPTY,
    Observable,
    catchError,
    forkJoin,
    map,
} from "rxjs";


interface BookRow extends Book {
    authorName: string;
    categoryName: string;
}

@Component({
    selector: 'app-book-list',
    imports: [
        AsyncPipe,
        ReactiveFormsModule,
        RouterLink,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatTableModule,
        MatTooltipModule,
        MatSnackBarModule
    ],
    templateUrl: './book-list.component.html',
    styleUrl: './book-list.component.scss',
})
export class BookList implements OnInit {
    private readonly categoryService = inject(CategoryService);
    private readonly authorService = inject(AuthorService);
    private readonly bookService = inject(BookService);
    private readonly snackBar = inject(MatSnackBar);
    private readonly formBuilder = inject(FormBuilder);

    private readonly confirmationDialog = inject(ConfirmationDialogService);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    books$!: Observable<BookRow[]>;

    readonly displayedColumns: string[] = [
        'title',
        'author',
        'category',
        'isbn',
        'publicationYear',
        'availability',
        'actions'
    ];

    readonly errorMessage = signal<string | null>(null);
    readonly isDeleting = signal<number | null>(null);
    readonly searchQuery = signal("");
    readonly searchForm = this.formBuilder.nonNullable.group({
        query: [''],
    });
    
    ngOnInit(): void {
        this.loadBooks();
    }

    search(): void {
        const query = this.searchForm.controls.query.value.trim();

        this.searchQuery.set(query);
        this.loadBooks(query);
    }

    clearSearch(): void {
        this.searchForm.controls.query.setValue('');
        this.searchQuery.set('');
        this.loadBooks();
    }

    deleteBook(book: BookRow): void {
        this.confirmationDialog.confirm({
            title: 'Brisanje knjige',
            message: `Da li želite da obrišete knjigu ${book.title}?`,
            confirmText: 'Obriši',
        }).subscribe((confirmed) => {
            if (!confirmed) {
                return;
            }
            this.performDelete(book);
        });
    }

    private performDelete(book: BookRow): void {
        this.errorMessage.set(null);
        this.isDeleting.set(book.id);

        this.bookService.delete(book.id).subscribe({
            next: () => {
                this.isDeleting.set(null);
                this.snackBar.open(
                    'Knjiga je uspešno obrisana.',
                    'Zatvori',
                    { duration: 3000 },
                );
                this.loadBooks(this.searchQuery());
            },
            error: (error: HttpErrorResponse) => {
                this.isDeleting.set(null);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Brisanje knjige nije uspelo.',
                    ),
                );
            },
        });
    }

    private loadBooks(query = ''): void {
        this.errorMessage.set(null);

        const booksRequest$ = query.trim()
            ? this.bookService.search(query)
            : this.bookService.getAll();

        this.books$ = forkJoin({
            books: booksRequest$,
            authors: this.authorService.getAll(),
            categories: this.categoryService.getAll()
        }).pipe( 
            map(({ books, authors, categories }) => {
                const authorNames = new Map<number, string>(
                    authors.map(
                        (author: Author): [number, string] =>
                            [author.id, `${author.firstName} ${author.lastName}`]
                    )
                );

                const categoryNames = new Map<number, string>(
                    categories.map(
                        (category: Category): [number, string] =>
                            [category.id, category.name]
                    )
                );

                return books.map((book) => ({
                    ...book,
                    authorName: authorNames.get(book.authorId) ?? `Autor #${book.authorId}`,
                    categoryName: categoryNames.get(book.categoryId) ?? `Kategorija #${book.categoryId}`,
                }));
            }),
            catchError((error: HttpErrorResponse) => {
                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje knjiga nije uspelo.',
                    ),
                );
                return EMPTY;
            }),
        );
    }
}
