import { Component, inject, OnInit, signal } from "@angular/core";
import { Book } from "../book.model";
import { AsyncPipe } from "@angular/common";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatListModule } from "@angular/material/list";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { BookService } from "../book.service";
import { AuthorService } from "../../authors/author.service";
import { CategoryService } from "../../categories/category.service";
import { Author } from "../../authors/author.model";
import { Category } from "../../categories/category.model";
import { HttpErrorResponse } from "@angular/common/http";
import { ApiErrorResponse } from "../../../core/auth/auth.models";

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
        MatListModule,
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

    books$!: Observable<BookRow[]>;
    
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
        const confirmed = window.confirm(
            `Da li zelite da obrisete knjigu "${book.title}"?`,
        );

        if (!confirmed) {
            return;
        }

        this.isDeleting.set(book.id);

        this.bookService.delete(book.id).subscribe({
            next: () => {
                this.isDeleting.set(null);
                this.snackBar.open('Knjiga je uspesno obrisana.', 'Zatvori', { duration: 3000 });
                this.loadBooks(this.searchQuery());
            },
            error: (error: HttpErrorResponse) => {
                this.isDeleting.set(null);

                const apiError = error.error as Partial<ApiErrorResponse> | null;

                this.errorMessage.set(
                    apiError?.message ?? 'Brisanje knjige nije uspelo.',
                );
            }
        })
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
                const apiError = error.error as Partial<ApiErrorResponse> | null;

                const message = error.status === 401 
                    ? 'Sesija nije validna. Odjavite se i ponovo prijavite.'
                    : error.status === 0
                    ? 'backend nije dostupan ili CORS nije podesen.'
                    : apiError?.message
                    ?? `Ucitavanje knjiga nije uspelo. Status: ${error.status}`;

                this.errorMessage.set(message);
                return EMPTY;
            }
        ));
    }
}
