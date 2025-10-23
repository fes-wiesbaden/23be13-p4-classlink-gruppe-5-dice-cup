import { bootstrapApplication } from '@angular/platform-browser';
// PrimeNG v20+: import core styles and a theme via PrimeUIX
// Use static imports so styles load with the initial bundle (avoid FOUC)
import '@primeuix/styles';
import '@primeuix/themes/aura';
import { AppComponent } from './app/app';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig);
