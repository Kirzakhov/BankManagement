import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BankRole, BankSessionService } from './bank';
import { AdminDashboard } from './admin-dashboard/admin-dashboard';
import { Login } from './login/login';
import { UserDashboard } from './user-dashboard/user-dashboard';

function roleGuard(role: BankRole) {
  return () => {
    const session = inject(BankSessionService).session();
    const router = inject(Router);
    return session?.role === role ? true : router.createUrlTree(['/login']);
  };
}

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'admin', component: AdminDashboard, canActivate: [roleGuard('ADMIN')] },
  { path: 'account', component: UserDashboard, canActivate: [roleGuard('USER')] },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' },
];
