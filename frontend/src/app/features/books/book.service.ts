import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Book, BookRequest } from './book.model';

@Injectable({
    providedIn: 'root',
})
export class BookService {
    private readonly http = inject(HttpClient);
    private readonly booksUrl = `${environment.apiUrl}/books`;

    getAll(): Observable<Book[]> {
        return this.http.get<Book[]>(this.booksUrl);
    }

    getById(id: number): Observable<Book> {
        return this.http.get<Book>(`${this.booksUrl}/${id}`);
    }

    search(query: string): Observable<Book[]> {
        return this.http.get<Book[]>(`${this.booksUrl}/search`, {
            params: { q: query.trim() },
        });
    }

    create(bookRequest: BookRequest): Observable<Book> {
        return this.http.post<Book>(this.booksUrl, bookRequest);
    }

    update(id: number, bookRequest: BookRequest): Observable<Book> {
        return this.http.put<Book>(`${this.booksUrl}/${id}`, bookRequest);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.booksUrl}/${id}`);
    }
}