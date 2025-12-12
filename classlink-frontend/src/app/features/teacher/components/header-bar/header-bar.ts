// Von Lukas bearbeitet
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { AppNotification } from '../../../../core/notifications/notification.model';
import { FormsModule } from '@angular/forms';

export interface ProjectOption {
  label: string;
  value: string;
}
export interface SimpleStudent {
  name: string;
  className: string;
  avatarUrl?: string;
}

@Component({
  standalone: true,
  selector: 'teacher-header-bar',
  imports: [CommonModule],
  templateUrl: './header-bar.html',
  styleUrl: './header-bar.scss',
})
export class TeacherHeaderBarComponent {
  private readonly notificationsSvc = inject(NotificationService);

  // Der Schüler, der oben in der Leiste angezeigt wird
  @Input() student!: SimpleStudent;
  @Input() average: number | null = null;
  @Input() trend: string | null = null;
  // Klick auf den Button: neues Projekt anlegen (Event nach außen)
  @Output() createProject = new EventEmitter<void>();

  imgFailed = false;
  // Falls das Bild kaputt ist, zeige ich den Initialen-Kreis
  markImgFailed() {
    this.imgFailed = true;
  }

  // Optional notification count (for bell badge)
  @Input() notifications = 0;

  showPanel = false;
  get items$(): Observable<AppNotification[]> {
    return this.notificationsSvc.items$;
  }
  get unreadCount$(): Observable<number> {
    return this.notificationsSvc.unreadCount$;
  }

  // Öffnet/Schließt das kleine Benachrichtigungsfenster
  togglePanel() {
    this.showPanel = !this.showPanel;
  }
  closePanel() {
    this.showPanel = false;
  }

  trackById(_: number, item: AppNotification) {
    return item.id;
  }

  // Markiert alle Benachrichtigungen als gelesen
  markAllRead() {
    this.notificationsSvc.markAllRead();
  }
}
