/*Datei von Lukas bearbeitet*/

import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private TK = 'app_token';
  private RK = 'app_roles';
  private UK = 'app_user';

  constructor() {
    // Beim Start prüfe ich kurz, ob schon ein Token da ist.
    // Wenn nicht, logge ich für die Demo automatisch als Lehrer ein.
    if (!localStorage.getItem(this.TK)) {
      this.login('dev-token', ['teacher'], 'dev-teacher');
    }
  }

  isLoggedIn(): boolean {
    // Hier schaue ich einfach, ob wir ein Token im Storage haben
    return !!localStorage.getItem(this.TK);
  }

  hasRole(r: string | string[]): boolean {
    // Prüft grob, ob mindestens eine der benötigten Rollen gesetzt ist
    const roles: string[] = JSON.parse(localStorage.getItem(this.RK) || '[]');
    const req = Array.isArray(r) ? r : [r];
    return req.some((x) => roles.includes(x));
  }

  login(token: string, roles: string[] = [], username?: string): void {
    // Speichere Token, Rollen und optional den Nutzernamen
    localStorage.setItem(this.TK, token);
    localStorage.setItem(this.RK, JSON.stringify(roles));
    if (username) {
      localStorage.setItem(this.UK, username);
    }
  }

  logout(): void {
    // Logout heißt: alles aus dem Storage wieder raus
    localStorage.removeItem(this.TK);
    localStorage.removeItem(this.RK);
    localStorage.removeItem(this.UK);
  }

  getUsername(): string | null {
    // Einfacher Getter, falls der Name angezeigt werden soll
    return localStorage.getItem(this.UK);
  }

  // For demo: update roles without changing token/username
  setRoles(roles: string[]): void {
    // Praktisch für die Demo, um schnell zwischen Rollen zu springen
    localStorage.setItem(this.RK, JSON.stringify(roles));
  }
}
