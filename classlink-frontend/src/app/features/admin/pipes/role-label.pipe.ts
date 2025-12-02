// Von Lukas bearbeitet
import { Pipe, PipeTransform } from '@angular/core';
import { Role } from '../models';

@Pipe({ name: 'roleLabel', standalone: true, pure: true })
export class RoleLabelPipe implements PipeTransform {
  // Kleine Übersetzung von den technischen Rollen zu einem Label
  transform(value: Role): string {
    switch (value) {
      case 'student':
        return 'Student';
      case 'teacher':
        return 'Teacher';
      case 'admin':
        return 'Admin';
      default:
        return String(value);
    }
  }
}
