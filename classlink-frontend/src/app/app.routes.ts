import { Routes } from '@angular/router';
import { StudentComponent } from './student/student';
import { TeacherComponent } from './teacher/teacher';
import { AdminComponent } from './admin/admin';

export const routes: Routes = [
  { path: 'student', component: StudentComponent },
  { path: 'teacher', component: TeacherComponent },
  { path: 'admin', component: AdminComponent },
  { path: '', redirectTo: '/student', pathMatch: 'full' },
  { path: '**', redirectTo: '/student' },
];
