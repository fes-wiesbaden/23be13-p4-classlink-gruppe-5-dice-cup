// Von Lukas bearbeitet
import {Component, EventEmitter, Input, Output, SimpleChanges, OnChanges} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {AdminUser} from '../../models';
import {RoleLabelPipe} from '../../pipes/role-label.pipe';
import {ButtonModule} from 'primeng/button';

@Component({
  standalone: true,
  selector: 'admin-user-table',
    imports: [CommonModule, FormsModule, RoleLabelPipe, ButtonModule],
  templateUrl: './user-table.html',
  styleUrl: './user-table.scss',
})
export class AdminUserTableComponent implements OnChanges {
  @Input() users: AdminUser[] = [];
  @Input() busy = false;

    @Output() deleteUser = new EventEmitter<string>();
    @Output() resetPassword = new EventEmitter<string>();

    ngOnChanges(changes: SimpleChanges) {
        if (changes['users']) {
            console.log('[AdminUserTable] users input', this.users);
        }
  }

  // Für *ngFor Performance
  trackById(_: number, u: AdminUser) {
    return u.id;
  }
}
