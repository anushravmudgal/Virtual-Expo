import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Home } from './pages/home/home';
import { OauthRedirect } from './pages/oauth-redirect/oauth-redirect';
import { Onboarding } from './pages/onboarding/onboarding';
import { Dashboard } from './pages/dashboard/dashboard';
import { ProjectGallery } from './pages/expo/project-gallery';
import { StudentDirectory } from './pages/expo/student-directory';
import { AdminLogin } from './pages/admin-login/admin-login';
import { AdminDashboard } from './pages/admin-dashboard/admin-dashboard';
import { adminGuard } from './guards/admin.guard';
import { ProjectDetails } from './pages/expo/project-details/project-details';

export const routes: Routes = [
  { path: '', redirectTo: 'Home', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'Home', component: Home },
  
  // This exactly matches the targetUrl we wrote in Spring Boot!
  { path: 'oauth2/redirect', component: OauthRedirect },
  { path: 'expo/projects/:id', component: ProjectDetails },
  { path: 'expo/students', component: StudentDirectory },
  { path: 'admin/dashboard', component: AdminDashboard, canActivate: [adminGuard] },
  { path: 'imsadmin', component: AdminLogin },
  { path: 'expo/projects', component: ProjectGallery },
  { path: 'admin/dashboard', component: AdminDashboard },
  { path: 'onboarding', component: Onboarding },
  { path: 'dashboard', component: Dashboard }
];


