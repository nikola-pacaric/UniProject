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
}