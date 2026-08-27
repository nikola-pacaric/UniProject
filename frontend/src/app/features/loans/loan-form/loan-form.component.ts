import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { Book } from '../../books/book.model';
import { BookService } from '../../books/book.service';
import { Member } from '../../members/member.model';
import { MemberService } from '../../members/member.service';
import { LoanRequest } from '../loan.model';
import { LoanService } from '../loan.service';

@Component({
    selector: 'app-loan-form',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatSelectModule,
        MatSnackBarModule,
    ],
    templateUrl: './loan-form.component.html',
    styleUrl: './loan-form.component.scss',
})
export class LoanForm implements OnInit {
    private readonly formBuilder = inject(FormBuilder);
    private readonly loanService = inject(LoanService);
    private readonly memberService = inject(MemberService);
    private readonly bookService = inject(BookService);
    private readonly router = inject(Router);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);
    private readonly snackBar = inject(MatSnackBar);

    readonly activeMembers = signal<Member[]>([]);
    readonly availableBooks = signal<Book[]>([]);
    readonly isLoading = signal(false);
    readonly isSubmitting = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly loanForm = this.formBuilder.nonNullable.group({
        memberId: [0, [Validators.required, Validators.min(1)]],
        bookId: [0, [Validators.required, Validators.min(1)]],
    });

    ngOnInit(): void {
        this.loadOptions();
    }

    submit(): void {
        if (this.loanForm.invalid) {
            this.loanForm.markAllAsTouched();
            return;
        }

        const request: LoanRequest = this.loanForm.getRawValue();

        this.isSubmitting.set(true);
        this.errorMessage.set(null);

        this.loanService.borrow(request).subscribe({
            next: () => {
                this.snackBar.open(
                    'Zaduženje je uspešno evidentirano.',
                    'Zatvori',
                    { duration: 3000 },
                );
                void this.router.navigate(['/loans']);
            },
            error: (error: HttpErrorResponse) => {
                this.isSubmitting.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Evidentiranje zaduženja nije uspelo.',
                    ),
                );
            },
        });
    }

    private loadOptions(): void {
        this.isLoading.set(true);
        this.errorMessage.set(null);

        forkJoin({
            members: this.memberService.getAll(),
            books: this.bookService.getAll(),
        }).subscribe({
            next: ({ members, books}) => {
                this.activeMembers.set(
                    members.filter((member) => member.active),
                );

                this.availableBooks.set(
                    books.filter((book) => book.availableCopies > 0),
                );

                this.isLoading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje opcija za zaduženje nije uspelo.',
                    ),
                );
            },
        });
    }
}