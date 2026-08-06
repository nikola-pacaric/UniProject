import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Author, AuthorRequest } from './author.model';

@Injectable({
    providedIn: 'root',
})
export class AuthorService {
    private readonly http = inject(HttpClient);
    private readonly authorsUrl = `${environment.apiUrl}/authors`;

    getAll(): Observable<Author[]> {
        return this.http.get<Author[]>(this.authorsUrl);
    }

    getById(id: number): Observable<Author> {
        return this.http.get<Author>(`${this.authorsUrl}/${id}`);
    }

    create(request: AuthorRequest): Observable<Author> {
        return this.http.post<Author>(this.authorsUrl, request);
    }

    update(id: number, request: AuthorRequest): Observable<Author> {
        return this.http.put<Author>(`${this.authorsUrl}/${id}`, request);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.authorsUrl}/${id}`);
    }
}