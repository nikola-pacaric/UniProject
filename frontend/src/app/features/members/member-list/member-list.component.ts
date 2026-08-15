import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core'
import { RouterLink } from '@angular/router';
import { EMPTY, Observable, catchError } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorResponse } from '../../../core/auth/auth.models';
import { Member } from '../member.model';
import { MemberService } from '../member.service';

@Component({
    selector: 'app-member-list',
    imports: [
        AsyncPipe,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatListModule,
        MatSnackBarModule
    ],
    templateUrl: './member-list.component.html',
    styleUrl: './member-list.component.scss',
})
export class MemberList implements OnInit {
    private readonly memberService = inject(MemberService);
    private readonly snackBar = inject(MatSnackBar);

    members$!: Observable<Member[]>;

    readonly errorMessage = signal<string | null>(null);
    readonly isChangingStatus = signal<number | null>(null);

    ngOnInit(): void {
        this.loadMembers();
    }

    toggleStatus(member: Member): void {
        this.errorMessage.set(null);
        this.isChangingStatus.set(member.id);

        const operation = member.active 
            ? this.memberService.deactivate(member.id)
            : this.memberService.activate(member.id);

        operation.subscribe({
            next: () => {
                this.isChangingStatus.set(null);

                this.snackBar.open(
                    member.active 
                        ? 'Clan je deaktiviran.'
                        : 'Clan je aktiviran.',
                    'Zatvori',
                    { duration: 3000 }
                );
                this.loadMembers();
            },
            error: (error: HttpErrorResponse) => {
                this.isChangingStatus.set(null);
                this.errorMessage.set(this.getErrorMessage(error));
            },
        });
    }

    private loadMembers(): void {
        this.errorMessage.set(null);

        this.members$ = this.memberService.getAll().pipe(
            catchError((error: HttpErrorResponse) => {
                this.errorMessage.set(this.getErrorMessage(error));
                return EMPTY;
            }),
        );
    }

    private getErrorMessage(error: HttpErrorResponse): string {
        const apiError = error.error as Partial<ApiErrorResponse> | null;

        return error.status == 401
            ? 'Sesija nije validna. Prijavte se ponovo.'
            : error.status === 0
                ? 'Backend nije dostupan ili CORS nije podesen.'
                : apiError?.message 
                ?? `ucitavanje clanova nije uspelo. Status: ${error.status}`;
    }
}