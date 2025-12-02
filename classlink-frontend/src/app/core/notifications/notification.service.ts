import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AppNotification } from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly _items$ = new BehaviorSubject<AppNotification[]>([
    {
      id: 'n1',
      title: 'Neue Bewertung',
      message: 'Anna hat eine Rückmeldung erhalten.',
      createdAt: new Date().toISOString(),
      read: false,
      type: 'info',
    },
    {
      id: 'n2',
      title: 'Projekt erstellt',
      message: 'Klimawandel wurde angelegt.',
      createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
      read: false,
      type: 'success',
    },
  ]);

  get items$(): Observable<AppNotification[]> {
    return this._items$.asObservable();
  }
  get unreadCount$(): Observable<number> {
    return this.items$.pipe(map((l) => l.filter((x) => !x.read).length));
  }

  markAllRead(): void {
    this._items$.next(this._items$.value.map((n) => ({ ...n, read: true })));
  }

  markRead(id: string): void {
    this._items$.next(this._items$.value.map((n) => (n.id === id ? { ...n, read: true } : n)));
  }

  add(item: AppNotification): void {
    this._items$.next([item, ...this._items$.value]);
  }

  // Placeholder for real-time hookup
  // connectWebSocket(url: string) { /* implement when backend exists */ }
}
