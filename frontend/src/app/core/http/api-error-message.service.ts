import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ApiErrorResponse } from '../auth/auth.models';

@Injectable({
  providedIn: 'root',
})
export class ApiErrorMessageService {
    private readonly exactTranslations: Readonly<Record<string, string>> = {
        'Username already taken' :
        'Korisničko ime je već zauzeto.',

        'Email already registered' :
        'Email adresa je već registrovana.',

        'Invalid username or password' :
        'Pogrešno korisničko ime ili lozinka.',

        'Validation failed' :
        'Uneti podaci nisu ispravni.',

        'Cannot delete author with associated books.' :
        'Autor ne može biti obrisan jer ima povezane knjige.',

        'Cannot delete category with associated books.' :
        'Kategorija ne može biti obrisana jer ima povezane knjige.',

        'Operation conflicts with existing data or related records.' :
        'Operacija nije moguća zbog postojećih ili povezanih podataka.',
    };

    getMessage(error: HttpErrorResponse, fallback: string): string {
        if (error.status === 0) {
            return 'Server nije dostupan. Proverite da li je pokrenut.';
        }

        const apiError = error.error as Partial<ApiErrorResponse> | null;

        const apiMessage = typeof apiError?.message === 'string' 
            ? apiError.message
            : '';
        
        const exactTranslation = this.exactTranslations[apiMessage];

        if (exactTranslation) {
            return exactTranslation;
        }

        if (apiMessage.startsWith('Cannot set total copies')) {
            return 'Ukupan broj primeraka ne može biti manji od broja trenutno zaduženih primeraka.';
        }

        if (apiMessage.startsWith('Cannot delete book')) {
            return 'Knjiga ne može biti obrisana jer ima aktivna zaduženja.';
        }

        if (apiMessage.startsWith('Cannot loan to inactive member')) {
            return 'Knjiga se ne može zadužiti neaktivnom članu.';
        }

        if (apiMessage.startsWith('No available copies')) {
            return 'Nema dostupnih primeraka izabrane knjige.';
        }

        if (
            apiMessage.startsWith('Loan with id')
            && apiMessage.includes('already returned')
        ) {
            return 'Ovo zaduženje je već vraćeno.';
        }

        if (apiMessage.startsWith('Cannot return loan')) {
            return 'Vraćanje ovog zaduženja trenutno nije moguće.';
        }

        if (error.status === 401) {
            return 'Sesija nije validna. Prijavite se ponovo.';
        }

        if (error.status === 404) {
            return 'Traženi podatak nije pronađen.';
        }

        if (error.status === 409) {
            return 'Operacija nije moguća zbog postojećih ili povezanih podataka.';
        }

        if (error.status === 400) {
            return 'Zahtev nije moguće izvršiti. Proverite unete podatke.';
        }

        return fallback;
    }
}