/*Datei von Lukas bearbeitet*/

import { CanMatchFn, Router, Route, UrlSegment } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanMatchFn = (route: Route, segs: UrlSegment[]) => {
  // Dieser Guard prüft, ob die Route bestimmte Rollen verlangt
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/login'], {
      queryParams: { redirectUrl: '/' + segs.map((s) => s.path).join('/') },
    });
  }

  // Aus den Routendaten hole ich mir die benötigten Rollen
  const need = (route.data?.['roles'] as string[]) ?? [];
  const have = auth.getRoles();
  console.log('[roleGuard] required:', need, 'have:', have);
  return need.length === 0 || auth.hasRole(need) ? true : router.createUrlTree(['/forbidden']);
};
