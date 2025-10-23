import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DevDockComponent } from './components/dev-dock/dev-dock';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, DevDockComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class AppComponent {
  // Dev auto-login handled centrally in AuthService (to avoid duplication)
  constructor() {}
}
