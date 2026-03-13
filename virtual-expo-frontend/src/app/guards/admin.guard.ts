import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const adminGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const adminName = localStorage.getItem('adminName');

  if (adminName) {
    return true; // Let them in!
  } else {
    router.navigate(['/imsadmin']); // Kick them back to login
    return false;
  }
};