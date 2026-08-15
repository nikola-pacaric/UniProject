import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Loan } from '../loans/loan.model';
import { Member, MemberRequest } from './member.model';

@Injectable({
    providedIn: 'root',
})
export class MemberService {
    private readonly http = inject(HttpClient);
    private readonly membersUrl = `${environment.apiUrl}/members`;

    getAll(): Observable<Member[]> {
        return this.http.get<Member[]>(this.membersUrl);
    }

    getById(id: number): Observable<Member> {
        return this.http.get<Member>(`${this.membersUrl}/${id}`);
    }

    create(request: MemberRequest): Observable<Member> {
        return this.http.post<Member>(this.membersUrl, request);
    }

    update(id: number, request: MemberRequest): Observable<Member> {
        return this.http.put<Member>(`${this.membersUrl}/${id}`, request)
    }

    deactivate(id: number): Observable<Member> {
        return this.http.patch<Member>(`${this.membersUrl}/${id}/deactivate`, {});
    }

    activate(id: number): Observable<Member> {
        return this.http.patch<Member>(`${this.membersUrl}/${id}/activate`, {});
    }

    getLoanHistory(memberId: number): Observable<Loan[]> {
        return this.http.get<Loan[]>(`${this.membersUrl}/${memberId}/loans`);
    }
}