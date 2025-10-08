import { Routes } from '@angular/router';
import { StudentComponent } from './routes/student/student';
import { TeacherComponent } from './routes/teacher/teacher';
import { AdminComponent } from './routes/admin/admin';
import { LoginComponent } from './routes/login/login';

export const routes: Routes = [
    { path: 'student', component: StudentComponent },
    { path: 'teacher', component: TeacherComponent },
    { path: 'admin', component: AdminComponent },
    { path: 'login', component: LoginComponent },
    { path: '', redirectTo: 'login', pathMatch: 'full'}
];
