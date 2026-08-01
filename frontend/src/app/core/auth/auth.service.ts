import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable , tap } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from './auth.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly authUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'uniproject.auth.token';

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, request).pipe(
      tap((response) => {
        if (response.token) {
          localStorage.setItem(this.tokenKey, response.token);
        } else {
          localStorage.removeItem(this.tokenKey);
        }
      }),
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/register`, request);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }
}