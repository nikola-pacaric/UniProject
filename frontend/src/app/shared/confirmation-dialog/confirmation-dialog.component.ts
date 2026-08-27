import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, } from '@angular/material/dialog'; 

export interface ConfirmationDialogData {
    title: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
}

@Component({
    selector: 'app-confirmation-dialog',
    imports: [
        MatDialogModule, 
        MatButtonModule
    ],
    templateUrl: './confirmation-dialog.component.html',
    styleUrl: './confirmation-dialog.component.scss',
})
export class ConfirmationDialog {
    readonly data = inject<ConfirmationDialogData>(MAT_DIALOG_DATA);
}