import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { map, Observable } from 'rxjs';

import { ConfirmationDialog, ConfirmationDialogData, } from './confirmation-dialog.component';

@Injectable({
    providedIn: 'root',
})
export class ConfirmationDialogService {
    private readonly dialog = inject(MatDialog);

    confirm(data: ConfirmationDialogData): Observable<boolean> {
        return this.dialog
            .open<ConfirmationDialog, ConfirmationDialogData, boolean>(ConfirmationDialog,
                {
                    data, 
                    role: 'alertdialog',
                    width: '440px',
                    maxWidth: 'calc(100vw - 32px)',
                    autoFocus: 'first-tabbable',
                },
            )
            .afterClosed()
            .pipe(map((confirmed) => confirmed === true));
    }
}