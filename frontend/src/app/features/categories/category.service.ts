import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Category, CategoryRequest } from './category.model';

@Injectable({ providedIn: 'root',})
export class CategoryService {
    private readonly http = inject(HttpClient);
    private readonly categoriesUrl = `${environment.apiUrl}/categories`;

    getAll(): Observable<Category[]> {
        return this.http.get<Category[]>(this.categoriesUrl);
    }

    getById(id: number): Observable<Category> {
        return this.http.get<Category>(`${this.categoriesUrl}/${id}`);
    }

    create(request: CategoryRequest): Observable<Category> {
        return this.http.post<Category>(this.categoriesUrl, request);
    }

    update(id: number, request: CategoryRequest): Observable<Category> {
        return this.http.put<Category>(`${this.categoriesUrl}/${id}`, request);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.categoriesUrl}/${id}`);
    }
}