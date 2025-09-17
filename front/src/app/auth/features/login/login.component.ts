import {Component, inject, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {AuthService} from "../../auth.service";
import {Router} from "@angular/router";
import {Button} from "primeng/button";
import {PasswordModule} from "primeng/password";
import {MessageModule} from "primeng/message";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    Button,
    PasswordModule,
    ReactiveFormsModule,
    MessageModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})


/**
 * Composant d'authentification (login).
 * - Utilise un formulaire réactif avec validations (email, password obligatoires).
 * - Appelle AuthService.login pour récupérer et stocker le JWT.
 * - Gère les états loading / error pour le retour utilisateur.
 */
export class LoginComponent {
  readonly form: FormGroup;
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  constructor() {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });
  }

  get f() {
    return this.form.controls;
  }

  /** Soumet les identifiants du formulaire */
  submit(): void {
    this.form.markAllAsTouched();
    this.error.set(null);

    if (this.form.invalid) return;

    const { email, password } = this.form.value;
    this.performLogin(email, password);
  }

  /**
   * Effectue l'appel d'authentification
   * et gère les états de chargement et d'erreur.
   */
  private performLogin(email: string, password: string): void {
    this.loading.set(true);

    this.auth.login({ email, password }).subscribe({
      next: () => {

        this.loading.set(false);
        this.router.navigateByUrl('/products/list');
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.readableError(err));
      },
    });
  }

  /** Transforme l'erreur HTTP en message lisible */
  private readableError(err: any): string {
    return (
      err?.error?.error ||
      err?.error?.businessErrorDescription ||
      err?.message ||
      'Invalid credentials'
    );
  }
}
