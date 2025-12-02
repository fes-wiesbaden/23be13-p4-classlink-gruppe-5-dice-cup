export type NotificationType = 'info' | 'success' | 'warning' | 'error';

export interface AppNotification {
  id: string;
  title: string;
  message?: string;
  createdAt: string; // ISO string
  read: boolean;
  type: NotificationType;
}
