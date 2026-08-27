import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { Loan } from '../../loans/loan.model';
import { Member } from '../member.model';
import { MemberService } from '../member.service';

@Component({
    selector: 'app-member-loan-history',
    imports: [
        RouterLink,
        MatButtonModule,
        MatTableModule
    ],
    templateUrl: './member-loan-history.component.html',
    styleUrl: './member-loan-history.component.scss',
})
export class MemberLoanHistory implements OnInit {
    private readonly memberService = inject(MemberService);
    private readonly route = inject(ActivatedRoute);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    readonly member = signal<Member | null>(null);
    readonly isLoading = signal(false);
    readonly loans = signal<Loan[]>([]);
    readonly errorMessage = signal<string | null>(null);

    readonly displayedColumns: string[] = [
        'book',
        'loanDate',
        'dueDate',
        'returnDate',
        'status',
    ];

    get memberId(): number | null {
        const value = this.route.snapshot.paramMap.get('id');
        return value ? Number(value) : null;
    }

    ngOnInit(): void {
        const id = this.memberId;

        if (id === null) {
            this.errorMessage.set('Član nije pronađen.');
            return;
        }

        this.isLoading.set(true);

        forkJoin({
            member: this.memberService.getById(id),
            loans: this.memberService.getLoanHistory(id),
        }).subscribe({
            next: ({ member, loans }) => {
                this.member.set(member);
                this.loans.set(loans);
                this.isLoading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje istorije zaduženja nije uspelo.',
                    ),
                );
            },
        });
    }

    formatDate(value: string | null): string {
        if (!value) {
            return '-';
        }

        const [year, month, day] = value.split('-');
        return `${day}.${month}.${year}.`;
    }

    getStatusLabel(loan: Loan): string {
        if (loan.status === 'RETURNED' || loan.returnDate) {
            return 'Vraćeno';
        }

        if (loan.status === 'OVERDUE') {
            return 'Zakasnelo';
        }

        const today = new Date().toISOString().slice(0, 10);
        return loan.dueDate < today ? 'Zakasnelo' : 'Aktivno';
    }
}