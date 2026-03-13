import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http'; // <-- 1. Import this
import { routes } from './app.routes'; 

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient() // <-- 2. Add this to wake up the API client!
  ]
};