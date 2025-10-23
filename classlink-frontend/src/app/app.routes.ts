/*Datei von Lukas bearbeitet*/


import { Routes } from '@angular/router';
import { StudentComponent } from './routes/student/student';
import { TeacherComponent } from './routes/teacher/teacher';
import { AdminComponent } from './routes/admin/admin';
import { LoginComponent } from './routes/login/login';

import { roleGuard } from '../guards/role.guard';
import { ForbiddenComponent } from '../forbidden/forbidden';  // korrekt: forbidden liegt auf derselben Ebene wie app
import { NotFoundComponent } from '../not_found/not_found';   // korrekt: not_found liegt auf derselben Ebene wie app

export const routes: Routes = [
    { path: '', pathMatch: 'full', redirectTo: 'login' },
    { path: 'login', component: LoginComponent },
    { path: 'student', component: StudentComponent, canMatch: [roleGuard], data: { roles: ['student'] } },
    { path: 'teacher', component: TeacherComponent, canMatch: [roleGuard], data: { roles: ['teacher'] } },
    { path: 'admin', component: AdminComponent, canMatch: [roleGuard], data: { roles: ['admin'] } },
    { path: 'forbidden', component: ForbiddenComponent },
    { path: '**', component: NotFoundComponent },
];
