import { Component } from '@angular/core';
import { Dock } from "primeng/dock";

@Component({
  standalone: true,
  selector: 'app-admin',
  imports: [Dock],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class AdminComponent {}
