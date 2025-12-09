import { Component, OnDestroy, computed, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Dock } from 'primeng/dock';
import { TooltipModule } from 'primeng/tooltip';
import { RippleModule } from 'primeng/ripple';
import { Router, NavigationEnd } from '@angular/router';
import { MenuItem, SharedModule } from 'primeng/api';
import { filter, Subscription, finalize } from 'rxjs';
import { AuthService } from '../../../services/auth.service';

type DockItem = MenuItem & {
  iconUrl: string;
  route: string;
  email?: string;
  password?: string;
};

@Component({
  selector: 'app-dev-dock',
  standalone: true,
  imports: [CommonModule, Dock, TooltipModule, RippleModule, SharedModule],
  templateUrl: './dev-dock.html',
  styleUrls: ['./dev-dock.scss'],
})
export class DevDockComponent implements OnDestroy {
  private router = inject(Router);
  private auth = inject(AuthService);

  private sub?: Subscription;
  currentUrl = signal<string>('/admin');
  busy = signal<boolean>(false);
  // DEV-ONLY: impersonation overlay handles switching; snapshot no longer needed

  constructor() {
    this.currentUrl.set(this.router.url);
    this.sub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => this.currentUrl.set(e.urlAfterRedirects || e.url));
  }
  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  // icons
  private base = [
    {
      label: 'Student',
      iconUrl: '/assets/icons/student.svg',
      route: '/student',
      email: 'max.mustermann@classlink.dev',
      password: 'Studi3sHard!',
    },
    {
      label: 'Teacher',
      iconUrl: '/assets/icons/teacher.svg',
      route: '/teacher',
      email: 'clara.lehrwerk@classlink.dev',
      password: 'Teach3rR0cks!',
    },
    {
      label: 'Admin',
      iconUrl: '/assets/icons/admin.svg',
      route: '/admin',
      // Admin bootstrap uses a random password; provide quick nav fallback
    },
    { label: 'Login', iconUrl: '/assets/icons/login.svg', route: '/login', roles: [] },
  ];

  items = computed<DockItem[]>(() => {
    const url = this.currentUrl();

    return this.base.map((i) => ({
      ...i,
      // outline highlight
      styleClass: url.startsWith(i.route) ? 'is-active' : undefined,
      tooltip: i.label,
      command: () => this.handleAction(i),
    }));
  });

  private handleAction(item: DockItem) {
    if (this.busy()) return;

    // Login icon: full logout (clears impersonation and base session)
    if (item.route === '/login' && !item.email && !item.password) {
      this.auth.logout();
      return;
    }

    if (item.email && item.password) {
      this.busy.set(true);
      console.log('[DevDock] impersonation login start', item.email);
      this.auth
        .impersonationLogin(item.email, item.password)
        .pipe(finalize(() => this.busy.set(false)))
        .subscribe({
          next: () => {
            const target = item.route;
            console.log('[DevDock] impersonation login success, navigating to', target);
            this.router.navigateByUrl(target).catch(console.error);
          },
          error: (err) => {
            console.error('[DevDock] impersonation login failed', err);
          },
        });
      return;
    }

    if (item.route === '/admin' && this.auth.isImpersonating()) {
      console.log('[DevDock] stopping impersonation, returning to base session');
      this.auth.stopImpersonation();
    }

    this.router.navigateByUrl(item.route).catch(console.error);
  }
}
