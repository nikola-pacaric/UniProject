import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';   

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { Author } from '../../authors/author.model';
import { AuthorService } from '../../authors/author.service';
import { Category } from '../../categories/category.model';
import { CategoryService } from '../../categories/category.service';
import { BookRequest } from '../book.model';
import { BookService } from '../book.service';

@Component({
    selector: 'app-book-form',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatSnackBarModule
    ],
    templateUrl: './book-form.component.html',
    styleUrl: './book-form.component.scss',
})
export class BookForm implements OnInit {
    private readonly bookService = inject(BookService);
    private readonly authorService = inject(AuthorService);
    private readonly categoryService = inject(CategoryService);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);  
    private readonly snackBar = inject(MatSnackBar);
    private readonly formBuilder = inject(FormBuilder);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    readonly authors = signal<Author[]>([]);
    readonly categories = signal<Category[]>([]);
    readonly isLoading = signal(false);
    readonly isSubmitting = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly bookForm = this.formBuilder.nonNullable.group({
        title: ['', [Validators.required, Validators.maxLength(255)]],
        isbn: ['', [Validators.required, Validators.maxLength(20)]],
        publicationYear: [new Date().getFullYear(), Validators.required],
        totalCopies: [1, [Validators.required, Validators.min(0)]],
        authorId: [0, [Validators.required, Validators.min(1)]],
        categoryId: [0, [Validators.required, Validators.min(1)]],
    });

    get bookId(): number | null {
        const value = this.route.snapshot.paramMap.get('id');
        return value ? Number(value) : null;
    }

    get isEditMode(): boolean {
        return this.bookId !== null;
    }

    ngOnInit(): void {
        this.isLoading.set(true);

        const id = this.bookId;
        const bookRequest$ = id === null
            ? of(null)
            : this.bookService.getById(id);

        forkJoin({
            authors: this.authorService.getAll(),
            categories: this.categoryService.getAll(),
            book: bookRequest$,
        }).subscribe({
            next: ({ authors, categories, book }) => {
                this.authors.set(authors);
                this.categories.set(categories);

                if (book) {
                    this.bookForm.patchValue({
                        title: book.title,
                        isbn: book.isbn,
                        publicationYear: book.publicationYear,
                        totalCopies: book.totalCopies,
                        authorId: book.authorId,
                        categoryId: book.categoryId,
                    });
                }
                this.isLoading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje podataka za knjigu nije uspelo.',
                    ),
                );
            },
        });
    }

    submit(): void {
        this.normalizeFields();

        if (this.bookForm.invalid) {
            this.bookForm.markAllAsTouched();
            return;
        }

        const values = this.bookForm.getRawValue();
        const request: BookRequest = {
            title: values.title,
            isbn: values.isbn,
            publicationYear: values.publicationYear,
            totalCopies: values.totalCopies,
            authorId: values.authorId,
            categoryId: values.categoryId,
        };

        this.isSubmitting.set(true);
        this.errorMessage.set(null);

        const id = this.bookId;
        const operation = id === null
            ? this.bookService.create(request)
            : this.bookService.update(id, request);

        operation.subscribe({
            next: () => {
                this.snackBar.open(
                    this.isEditMode
                        ? 'Knjiga je uspešno izmenjena.'
                        : 'Knjiga je uspešno kreirana.',
                    'Zatvori',
                    { duration: 3000 },
                );
                void this.router.navigate(['/books']);
            },
            error: (error: HttpErrorResponse) => {
                this.isSubmitting.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Čuvanje knjige nije uspelo.',
                    ),
                );
            },
        });
    }

    private normalizeFields(): void {
        this.bookForm.patchValue({
            title: this.bookForm.controls.title.value.trim(),
            isbn: this.bookForm.controls.isbn.value.trim(),
        });
    }
}