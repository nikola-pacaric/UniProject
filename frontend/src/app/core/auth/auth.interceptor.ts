import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  const isApiRequest = request.url.startsWith(`${environment.apiUrl}/`,);

  const isPublicAuthRequest = 
    request.url === `${environment.apiUrl}/auth/login` || 
    request.url === `${environment.apiUrl}/auth/register`;

  const requestToSend = token && isApiRequest && !isPublicAuthRequest ? request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    }
  }) : request;

  if (!isApiRequest || isPublicAuthRequest) {
    return next(requestToSend);
  }

  return next(requestToSend).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();

        if (router.url !== '/login') {
          void router.navigate(['/login'], {
            queryParams: {
              returnUrl: router.url,
            }
          });
        }
      }

      return throwError(() => error);
    }),
  );
};
