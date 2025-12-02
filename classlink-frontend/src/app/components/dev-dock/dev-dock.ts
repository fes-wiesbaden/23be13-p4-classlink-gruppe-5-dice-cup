import { Component, OnDestroy, computed, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Dock } from 'primeng/dock';
import { TooltipModule } from 'primeng/tooltip';
import { RippleModule } from 'primeng/ripple';
import { Router, NavigationEnd } from '@angular/router';
import { MenuItem, SharedModule } from 'primeng/api';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../../../services/auth.service';

type DockItem = MenuItem & { iconUrl: string };

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
      roles: ['student'] as string[],
    },
    {
      label: 'Teacher',
      iconUrl: '/assets/icons/teacher.svg',
      route: '/teacher',
      roles: ['teacher'] as string[],
    },
    {
      label: 'Admin',
      iconUrl: '/assets/icons/admin.svg',
      route: '/admin',
      roles: ['admin'] as string[],
    },
    { label: 'Login', iconUrl: '/assets/icons/login.svg', route: '/login', roles: [] },
  ];

  items = computed<DockItem[]>(() => {
    const url = this.currentUrl();

    // Always show all dock items, including Login
    const visible = this.base;

    return visible.map((i) => ({
      label: i.label,
      iconUrl: i.iconUrl,
      // outline highlight
      styleClass: url.startsWith(i.route) ? 'is-active' : undefined,
      tooltip: i.label,
      command: () => {
        if ('roles' in i) this.auth.setRoles((i as any).roles);
        this.router.navigateByUrl(i.route);
      },
    }));
  });
}
