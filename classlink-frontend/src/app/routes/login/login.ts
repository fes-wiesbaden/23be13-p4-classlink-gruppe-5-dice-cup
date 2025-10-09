import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';
import { Password } from 'primeng/password';
import { Checkbox } from 'primeng/checkbox';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, Card, InputText, Password, Checkbox, Button],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  readonly loginForm: FormGroup;
  submitted = false;
  decorActive = false;
  isSubmitting = false;

  constructor(private readonly fb: FormBuilder) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false],
    });
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
    const credentials = this.loginForm.value;
    console.info('Login submitted', credentials);

    setTimeout(() => {
      this.isSubmitting = false;
    }, 1200);
  }

  get usernameControl() {
    return this.loginForm.get('username');
  }

  get passwordControl() {
    return this.loginForm.get('password');
  }
}




