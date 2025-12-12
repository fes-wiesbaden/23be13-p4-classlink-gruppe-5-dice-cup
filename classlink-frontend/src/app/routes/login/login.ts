// Author: Emil
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
    errorMessage: string | null = null;
  // Deaktiviert Transitions/Animationen für den allerersten Paint
  noAnim = true;

  constructor() {
    this.loginForm = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
        role: ['teacher'],
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
      const {email, password} = this.loginForm.value as { email: string; password: string };
      this.auth
          .login(email, password)
          .subscribe({
              next: () => {
                  this.isSubmitting = false;
                  this.errorMessage = null;
                  const redirect = this.route.snapshot.queryParamMap.get('redirectUrl');
                  const fallback = '/admin';
                  this.router.navigateByUrl(redirect || fallback).catch(console.error);
              },
              error: (err) => {
                  console.error('Login failed', err);
                  this.isSubmitting = false;
                  this.errorMessage = 'Login fehlgeschlagen. Bitte prüfen Sie Ihre Eingaben.';
              },
          });
  }

    get emailControl() {
        return this.loginForm.get('email');
  }

  get passwordControl() {
    return this.loginForm.get('password');
  }

  // role selection removed; roles managed via DevDock for demo
}
