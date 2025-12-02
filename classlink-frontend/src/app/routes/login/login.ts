import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';
import { Password } from 'primeng/password';
import { Checkbox } from 'primeng/checkbox';
import { AuthService } from '../../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, Card, InputText, Password, Checkbox, Button],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loginForm: FormGroup;
  submitted = false;
  // Start im aktiven Look, damit die Karte/Orbs sofort "an" sind
  decorActive = true;
  isSubmitting = false;
  // Deaktiviert Transitions/Animationen für den allerersten Paint
  noAnim = true;

  constructor() {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false],
    });

    // Nach dem ersten Tick Transitions wieder erlauben
    setTimeout(() => {
      this.noAnim = false;
    }, 0);
  }

  activateDecor(): void {
    this.decorActive = true;
  }

  deactivateDecor(): void {
    this.decorActive = false;
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const { username } = this.loginForm.value as { username: string };

    // Demo-/Stub-Login: erzeugt ein Fake-Token und setzt Default-Rolle 'teacher'
    const fakeToken = `dc.${btoa(username || 'user')}.${Date.now()}`;
    this.auth.login(fakeToken, ['teacher'], username);

    // Redirect auf gewünschte Zielseite (QueryParam) oder rollenbasiertes Standardziel
    const redirect = this.route.snapshot.queryParamMap.get('redirectUrl');
    const fallback = '/teacher';

    // kleine Verzögerung für visuelles Feedback
    setTimeout(() => {
      this.isSubmitting = false;
      this.router.navigateByUrl(redirect || fallback);
    }, 300);
  }

  get usernameControl() {
    return this.loginForm.get('username');
  }

  get passwordControl() {
    return this.loginForm.get('password');
  }

  // role selection removed; roles managed via DevDock for demo
}
