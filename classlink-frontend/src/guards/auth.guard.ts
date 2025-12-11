/*Datei von Lukas bearbeitet*/
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_r, s) => {
  // Wenn nicht eingeloggt, leite ich zur Login-Seite um
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { redirectUrl: s.url } });
};
