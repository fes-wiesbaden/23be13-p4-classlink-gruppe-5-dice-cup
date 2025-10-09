import { Component, OnDestroy, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Dock } from 'primeng/dock';
import { TooltipModule } from 'primeng/tooltip';
import { RippleModule } from 'primeng/ripple';
import { Router, NavigationEnd } from '@angular/router';
import { MenuItem, SharedModule } from 'primeng/api';
import { filter, Subscription } from 'rxjs';

type DockItem = MenuItem & { iconUrl: string };

@Component({
  selector: 'app-dev-dock',
  standalone: true,
  imports: [CommonModule, Dock, TooltipModule, RippleModule, SharedModule],
  templateUrl: './dev-dock.html',
  styleUrls: ['./dev-dock.scss']
})
export class DevDockComponent implements OnDestroy {
  private sub?: Subscription;
  currentUrl = signal<string>('/admin');

  constructor(private router: Router) {
    this.currentUrl.set(this.router.url);
    this.sub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(e => this.currentUrl.set(e.urlAfterRedirects || e.url));
  }
  ngOnDestroy() { this.sub?.unsubscribe(); }

  // icons
  private base = [
    { label: 'Student', iconUrl: '/assets/icons/student.svg', route: '/student' },
    { label: 'Teacher', iconUrl: '/assets/icons/teacher.svg', route: '/teacher' },
    { label: 'Admin',   iconUrl: '/assets/icons/admin.svg',   route: '/admin'   },
    { label: 'Login',   iconUrl: '/assets/icons/login.svg',   route: '/login'   },
  ];

  items = computed<DockItem[]>(() => {
    const url = this.currentUrl();
    return this.base.map(i => ({
      label: i.label,
      iconUrl: i.iconUrl,
      // outline highlite
      styleClass: url.startsWith(i.route) ? 'is-active' : undefined,
      tooltip: i.label,
      command: () => this.router.navigateByUrl(i.route)
    }));
  });
}
