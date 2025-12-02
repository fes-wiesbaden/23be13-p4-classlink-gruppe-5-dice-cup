import { InjectionToken } from '@angular/core';

// Base URL for backend API; can be overridden in appConfig providers
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => '/api',
});
