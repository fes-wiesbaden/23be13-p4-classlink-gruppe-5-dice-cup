import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { LoginComponent } from './routes/login/login';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
  ],
};
