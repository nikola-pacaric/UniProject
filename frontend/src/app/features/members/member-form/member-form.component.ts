import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiErrorMessageService } from '../../../core/http/api-error-message.service';
import { MemberRequest } from '../member.model';
import { MemberService } from '../member.service';

@Component({
    selector: 'app-member-form',
    imports: [
        ReactiveFormsModule,
        RouterLink,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule
    ],
    templateUrl: './member-form.component.html',
    styleUrl: './member-form.component.scss'
})
export class MemberForm implements OnInit {
    private readonly formBuilder = inject(FormBuilder);
    private readonly memberService = inject(MemberService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly snackBar = inject(MatSnackBar);
    private readonly apiErrorMessage = inject(ApiErrorMessageService);

    readonly isLoading = signal(false);
    readonly isSubmitting = signal(false);
    readonly errorMessage = signal<string | null>(null);

    readonly memberForm = this.formBuilder.nonNullable.group({
        firstName: ['', [Validators.required, Validators.maxLength(100)]],
        lastName: ['', [Validators.required, Validators.maxLength(100)]],
        membershipCardNumber: ['', [Validators.required, Validators.maxLength(50)]],
        email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
        phone: ['', [Validators.maxLength(50)]],
    });

    get memberId(): number | null {
        const value = this.route.snapshot.paramMap.get('id');
        return value ? parseInt(value) : null;
    }

    get isEditMode(): boolean {
        return this.memberId !== null;
    }

    ngOnInit(): void {
        const id = this.memberId;

        if (id === null) {
            return;
        }

        this.isLoading.set(true);

        this.memberService.getById(id).subscribe({
            next: (member) => {
                this.memberForm.patchValue({
                    firstName: member.firstName,
                    lastName: member.lastName,
                    membershipCardNumber: member.membershipCardNumber,
                    email: member.email,
                    phone: member.phone ?? '',
                });
                this.isLoading.set(false);
            },
            error: (error: HttpErrorResponse) => {
                this.isLoading.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Učitavanje člana nije uspelo.',
                    ),
                );
            },
        });
    }

    submit(): void {
        this.normalizeFields();

        if (this.memberForm.invalid) {
            this.memberForm.markAllAsTouched();
            return;
        }

        const values = this.memberForm.getRawValue();
        const request: MemberRequest = {
            firstName: values.firstName,
            lastName: values.lastName,
            membershipCardNumber: values.membershipCardNumber,
            email: values.email,
            phone: values.phone || null,
        };

        this.isSubmitting.set(true);
        this.errorMessage.set(null);

        const id = this.memberId;
        const operation = id === null 
            ? this.memberService.create(request)
            : this.memberService.update(id, request);

        operation.subscribe({
            next: () => {
                this.snackBar.open(
                    this.isEditMode
                        ? 'Član je uspešno izmenjen.'
                        : 'Član je uspešno kreiran.',
                    'Zatvori',
                    { duration: 3000 }
                );
                void this.router.navigate(['/members']);
            },
            error: (error: HttpErrorResponse) => {
                this.isSubmitting.set(false);

                this.errorMessage.set(
                    this.apiErrorMessage.getMessage(
                        error,
                        'Čuvanje člana nije uspelo.',
                    ),
                );
            },
        });
    }

    private normalizeFields(): void {
        this.memberForm.patchValue({
            firstName: this.memberForm.controls.firstName.value.trim() ,
            lastName: this.memberForm.controls.lastName.value.trim(),
            membershipCardNumber: this.memberForm.controls.membershipCardNumber.value.trim(),
            email: this.memberForm.controls.email.value.trim(),
            phone: this.memberForm.controls.phone.value.trim(),
        });
    }
}