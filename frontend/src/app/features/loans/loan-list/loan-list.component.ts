import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EMPTY, Observable, catchError, forkJoin, map } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorResponse } from '../../../core/auth/auth.models';
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
        MatListModule,
        MatIconModule,
        MatSnackBarModule,
        AsyncPipe
    ],
    templateUrl: './loan-list.component.html',
    styleUrl: './loan-list.component.scss',
})
export class LoanList implements OnInit {
    private readonly loanService = inject(LoanService);
    private readonly memberService = inject(MemberService);
    private readonly snackBar = inject(MatSnackBar);

    loans$!: Observable<LoanRow[]>;

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
        const confirmed = window.confirm(
            `Da li zelite da evidentirate povratak knjige "${loan.bookTitleAtLoan}"?`,
        );

        if (!confirmed) {
            return;
        }

        this.errorMessage.set(null);
        this.isReturning.set(loan.id);

        this.loanService.returnLoan(loan.id).subscribe({
            next: () => {
                this.isReturning.set(null);
                this.snackBar.open(
                    'Povratak knjige je evidentiran.',
                    'Zatvori',
                    { duration: 3000 },
                );
                this.loadLoans(this.showOverdueOnly());
            },
            error: (error: HttpErrorResponse) => {
                this.isReturning.set(null);

                const apiError = error.error as Partial<ApiErrorResponse> | null;
                this.errorMessage.set(
                    apiError?.message
                        ?? 'Povratak knjige nije uspeo.',
                );
            },
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
            return 'Vraceno';
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
                        ?? `Clan #${loan.memberId}`,
                }));
            }),
            catchError((error: HttpErrorResponse) => {
                const apiError = error.error as Partial<ApiErrorResponse> | null;

                const fallbackMesage = overdueOnly 
                    ? `Ucitavanje zakasnelih zaduzenja nije uspelo. Status: ${error.status}`
                    : `Ucitavanje zaduzenja nije uspelo. Status: ${error.status}`;

                const message = error.status === 401
                    ? 'Sesija nije validna. Prijavite se ponovo.'
                    : error.status === 0
                        ? 'Backend nije dostupan ili CORS nije podesen.'
                        : apiError?.message
                            ?? fallbackMesage;

                this.errorMessage.set(message);
                return EMPTY;
            }),
        );
    }
}