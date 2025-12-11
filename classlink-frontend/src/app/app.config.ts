import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/http/auth.interceptor';
import { BASE_PATH } from './api';
import { API_BASE_URL } from './core/api.tokens';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: BASE_PATH, useValue: 'http://localhost:4000' },
    { provide: API_BASE_URL, useValue: 'http://localhost:4000' },
  ],
};
