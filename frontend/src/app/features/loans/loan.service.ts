import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Loan, LoanRequest } from './loan.model';

@Injectable({
    providedIn: 'root',
})
export class LoanService {
    private readonly http = inject(HttpClient);
    private readonly loansUrl = `${environment.apiUrl}/loans`;

    getAll(): Observable<Loan[]> {
        return this.http.get<Loan[]>(this.loansUrl);
    }

    getById(id: number): Observable<Loan> {
        return this.http.get<Loan>(`${this.loansUrl}/${id}`);
    }

    borrow(request: LoanRequest): Observable<Loan> {
        return this.http.post<Loan>(this.loansUrl, request);
    }

    returnLoan(id: number): Observable<Loan> {
        return this.http.post<Loan>(`${this.loansUrl}/${id}/return`, {});
    }

    getOverdue(): Observable<Loan[]> {
        return this.http.get<Loan[]>(`${this.loansUrl}/overdue`);
    }
}