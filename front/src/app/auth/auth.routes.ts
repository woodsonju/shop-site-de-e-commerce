import { Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import { RegisterComponent } from './features/register/register.component';
import { ActivateAccountComponent } from './features/activate-account/activate-account.component';

export const AUTH_ROUTES: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'activate-account', component: ActivateAccountComponent },
  { path: '**', redirectTo: 'login' }
];
