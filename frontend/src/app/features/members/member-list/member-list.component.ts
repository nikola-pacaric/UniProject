import { AsyncPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core'
import { RouterLink } from '@angular/router';
import { EMPTY, Observable, catchError } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ConfirmationDialogService } from '../../../shared/confirmation-dialog/confirmation-dialog.service';

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { Member } from '../member.model';
import { MemberService } from '../member.service';

@Component({
    selector: 'app-member-list',
    imports: [
        AsyncPipe,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatTableModule,
        MatSnackBarModule,
        MatTooltipModule,
    ],
    templateUrl: './member-list.component.html',
    styleUrl: './member-list.component.scss',
})
export class MemberList implements OnInit {
    private readonly memberService = inject(MemberService);
    private readonly snackBar = inject(MatSnackBar);
    private readonly confirmationDialog = inject(ConfirmationDialogService);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    members$!: Observable<Member[]>;

    readonly displayedColumns: string[] = [
        'name',
        'cardNumber',
        'email',
        'phone',
        'status',
        'actions',
    ];

    readonly errorMessage = signal<string | null>(null);
    readonly isChangingStatus = signal<number | null>(null);

    ngOnInit(): void {
        this.loadMembers();
    }

    toggleStatus(member: Member): void {
        if (!member.active) {
            this.changeStatus(member);
            return;
        }

        this.confirmationDialog.confirm({
            title: 'Deaktivacija člana',
            message: `Da li želite da deaktivirate člana ${member.firstName} ${member.lastName}?`,
            confirmText: 'Deaktiviraj',
        }).subscribe((confirmed) => {
            if (!confirmed) {
                return;
            }
            this.changeStatus(member);
        });
    }

    private changeStatus(member: Member): void {
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
                        ? 'Član je deaktiviran.'
                        : 'Član je aktiviran.',
                    'Zatvori',
                    { duration: 3000 },
                );
                this.loadMembers();
            },
            error: (error: HttpErrorResponse) => {
                this.isChangingStatus.set(null);
                
                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Promena statusa člana nije uspela.',
                    ),
                );
            },
        });
    }

    private loadMembers(): void {
        this.errorMessage.set(null);

        this.members$ = this.memberService.getAll().pipe(
            catchError((error: HttpErrorResponse) => {
                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje članova nije uspelo.',
                    ),
                );
                return EMPTY;
            }),
        );
    }
}