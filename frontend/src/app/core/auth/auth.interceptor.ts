import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const isApiRequest = request.url.startsWith(`${environment.apiUrl}/`,);

  const isPublicAuthRequest = request.url === `${environment.apiUrl}/auth/login` || request.url === `${environment.apiUrl}/auth/register`;

  if (!token || !isApiRequest || isPublicAuthRequest) {
    return next(request);
  }

  const authorizedRequest = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authorizedRequest);
};
