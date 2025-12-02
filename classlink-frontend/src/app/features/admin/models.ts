// Von Lukas bearbeitet
export type Role = 'student' | 'teacher' | 'admin';

export type AdminUser = {
  id: number;
  name: string;
  email: string;
  roles: Role[];
  status: 'active' | 'disabled';
  createdAt: string; // ISO date
};
