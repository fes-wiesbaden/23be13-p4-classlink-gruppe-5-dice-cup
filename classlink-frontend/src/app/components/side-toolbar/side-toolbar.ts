import { Component, OnDestroy, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter, Subscription } from 'rxjs';

interface Item {
  icon: string;
  route: string;
  tooltip: string;
}

@Component({
  standalone: true,
  selector: 'app-side-toolbar',
  imports: [CommonModule],
  templateUrl: './side-toolbar.html',
  styleUrl: './side-toolbar.scss',
})
export class SideToolbarComponent implements OnDestroy {
  private router = inject(Router);

  private sub?: Subscription;
  currentUrl = signal<string>('/');

  constructor() {
    this.currentUrl.set(this.router.url);
    this.sub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => this.currentUrl.set(e.urlAfterRedirects || e.url));
  }
  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  base: Item[] = [
    { icon: 'pi-th-large', route: '/student', tooltip: 'Dashboard' },
    { icon: 'pi-file', route: '/login', tooltip: 'Dokumente' },
    { icon: 'pi-users', route: '/teacher', tooltip: 'Personen' },
    { icon: 'pi-cog', route: '/admin', tooltip: 'Einstellungen' },
  ];

  items = computed(() =>
    this.base.map((i) => ({
      ...i,
      active: this.currentUrl().startsWith(i.route),
    })),
  );

  go(route: string) {
    this.router.navigateByUrl(route);
  }
}
