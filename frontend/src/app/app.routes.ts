import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';
import { Login } from './features/auth/login/login.component';
import { Register } from './features/auth/register/register.component';
import { AppShell } from './layout/app-shell/app-shell.component';

export const routes: Routes = [
    {
        path: 'login',
        component: Login,
    },
    {
        path: 'register',
        component: Register,
    },
    {
        path: '',
        component: AppShell,
        canActivate: [authGuard],
        pathMatch: 'full',
    },
    {
        path: '**',
        redirectTo: '',
    }
];
