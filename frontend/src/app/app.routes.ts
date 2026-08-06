import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';
import { Login } from './features/auth/login/login.component';
import { Register } from './features/auth/register/register.component';
import { AppShell } from './layout/app-shell/app-shell.component';
import { AuthorList } from './features/authors/author-list/author-list.component';
import { AuthorForm } from './features/authors/author-form/author-form.component';

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
        children: [
            {
                path: '',
                redirectTo: 'authors',
                pathMatch: 'full',
            },
            {
                path: 'authors',
                children: [
                    {
                        path: '',
                        component: AuthorList,
                    },
                    {
                        path: 'new',
                        component: AuthorForm,
                    },
                    {
                        path: ':id/edit',
                        component: AuthorForm,
                    }
                ]
            }
        ],
    },
    {
        path: '**',
        redirectTo: '',
    }
];
