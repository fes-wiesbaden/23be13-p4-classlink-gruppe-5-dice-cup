import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule, TableModule, TagModule, ProgressSpinnerModule],
  template: `
    <p-table
      [value]="logs"
      [paginator]="paginator"
      [rows]="rows"
      [rowsPerPageOptions]="rowsPerPageOptions"
      [sortField]="defaultSortField"
      [sortOrder]="-1"
      dataKey="id"
      [tableStyle]="{ 'min-width': '50rem' }"
    >
      <ng-template pTemplate="header">
        <tr>
          <th pSortableColumn="createdAt">Date <p-sortIcon field="createdAt"></p-sortIcon></th>
          <th pSortableColumn="action">Action <p-sortIcon field="action"></p-sortIcon></th>
          <th>Entity</th>
          <th>Actor</th>
          <th>Severity</th>
        </tr>
      </ng-template>

      <ng-template pTemplate="body" let-row>
        <tr (click)="rowClick.emit(row)">
          <td>{{ row.createdAt | date:'medium' }}</td>
          <td>{{ row.action }}</td>
          <td>{{ row.entity || '—' }}</td>
          <td><code>{{ row.actorId }}</code></td>
          <td>
            <p-tag [value]="row.severity || 'info'" [severity]="tagSeverity(row.severity)"></p-tag>
          </td>
        </tr>
      </ng-template>

      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="5" class="text-center">
            <ng-container *ngIf="!loading; else loadingTpl">
              No audit logs found.
            </ng-container>
            <ng-template #loadingTpl>
              <p-progressSpinner styleClass="p-d-block mx-auto" style="width:1.5em;height:1.5em"></p-progressSpinner>
            </ng-template>
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
  styleUrl: './audit-log.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuditLog {
  @Input() logs: any[] = [];
  @Input() loading = false;
  @Input() paginator = false;
  @Input() rows = 10;
  @Input() rowsPerPageOptions: number[] = [10, 25, 50];
  @Input() defaultSortField = 'createdAt';

  @Output() rowClick = new EventEmitter<any>();

  tagSeverity(severity: string): string {
    switch ((severity || '').toLowerCase()) {
      case 'info':
        return 'info';
      case 'warn':
      case 'warning':
        return 'warning';
      case 'error':
        return 'danger';
      case 'success':
        return 'success';
      default:
        return 'info';
    }
  }
}
