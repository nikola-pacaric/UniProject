import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EMPTY, Observable, catchError, forkJoin, map } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ConfirmationDialogService } from '../../../shared/confirmation-dialog/confirmation-dialog.service';

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { Member } from '../../members/member.model';
import { MemberService } from '../../members/member.service';
import { Loan } from '../loan.model';
import { LoanService } from '../loan.service';

interface LoanRow extends Loan {
    memberName: string;
}

@Component({
    selector: 'app-loan-list',
    imports: [
        RouterLink,
        MatButtonModule,
        MatTableModule,
        MatIconModule,
        MatSnackBarModule,
        AsyncPipe,
        MatTooltipModule,
    ],
    templateUrl: './loan-list.component.html',
    styleUrl: './loan-list.component.scss',
})
export class LoanList implements OnInit {
    private readonly loanService = inject(LoanService);
    private readonly memberService = inject(MemberService);
    private readonly snackBar = inject(MatSnackBar);
    private readonly confirmationDialog = inject(ConfirmationDialogService);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    loans$!: Observable<LoanRow[]>;

    readonly displayedColumns: string[] = [
        'book',
        'member',
        'loanDate',
        'dueDate',
        'returnDate',
        'status',
        'actions',
    ];

    readonly errorMessage = signal<string | null>(null);
    readonly isReturning = signal<number | null>(null);
    readonly showOverdueOnly = signal(false);

    ngOnInit(): void {
        this.loadLoans();
    }

    showAllLoans(): void {
        this.loadLoans(false);
    }

    showOverdueLoans(): void {
        this.loadLoans(true);
    }

    returnLoan(loan: LoanRow): void {
        this.confirmationDialog.confirm({
            title: 'Vraćanje knjige',
            message: `Da li želite da vratite knjigu "${loan.bookTitleAtLoan}"?`,
            confirmText: 'Evidentiraj vraćanje',
        }).subscribe((confirmed) => {
            if (!confirmed) {
                return;
            }
            this.performReturn(loan);
        });
    }

    isReturned(loan: Loan): boolean {
        return loan.status === 'RETURNED' || loan.returnDate !== null;
    }

    isOverdue(loan: Loan): boolean {
        const today = new Date().toISOString().slice(0, 10);

        return !this.isReturned(loan)
            && (loan.status === 'OVERDUE' || loan.dueDate < today);
    }

    canReturn(loan: Loan): boolean {
        return !this.isReturned(loan);
    }

    getStatusLabel(loan: Loan): string {
        if (this.isReturned(loan)) {
            return 'Vraćeno';
        }

        return this.isOverdue(loan) ? 'Zakasnelo' : 'Aktivno';
    }

    formatDate(value: string | null): string {
        if (!value) {
            return '-';
        }

        const [year, month, day] = value.split('-');
        return `${day}.${month}.${year}.`;
    }

    private performReturn(loan: LoanRow): void {
        this.errorMessage.set(null);
        this.isReturning.set(loan.id);

        this.loanService.returnLoan(loan.id).subscribe({
            next: () => {
                this.isReturning.set(null);
                this.snackBar.open(
                    'Vraćanje knjige je evidentirano.',
                    'Zatvori',
                    { duration: 3000 },
                );
                this.loadLoans(this.showOverdueOnly());
            },
            error: (error: HttpErrorResponse) => {
                this.isReturning.set(null);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Vraćanje knjige nije uspelo.',
                    ),
                );
            },
        });
    }

    private loadLoans(overdueOnly = false): void {
        this.errorMessage.set(null);
        this.showOverdueOnly.set(overdueOnly);

        const loansRequest$ = overdueOnly
            ? this.loanService.getOverdue()
            : this.loanService.getAll();
        
        this.loans$ = forkJoin({
            loans: loansRequest$,
            members: this.memberService.getAll(),
        }).pipe(
            map(({loans, members}) => {
                const memberNames = new Map<number, string>(
                    members.map((member: Member): [number, string] => [
                        member.id, `${member.firstName} ${member.lastName}`
                    ]),
                );
                return loans.map((loan) => ({
                    ...loan,
                    memberName:
                        memberNames.get(loan.memberId) 
                        ?? `Član #${loan.memberId}`,
                }));
            }),
            catchError((error: HttpErrorResponse) => {
                const fallbackMessage = overdueOnly
                    ? 'Učitavanje zakasnelih zaduženja nije uspelo.'
                    : 'Učitavanje zaduženja nije uspelo.';

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        fallbackMessage,
                    ),
                );  
                return EMPTY;
            }),
        );
    }
}