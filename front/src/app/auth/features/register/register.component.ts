import {Component, inject, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {TokenService} from "../../token.service";
import {AuthService} from "../../auth.service";
import {MessageModule} from "primeng/message";
import {PasswordModule} from "primeng/password";
import {Button} from "primeng/button";
import {ChipsModule} from "primeng/chips";

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    MessageModule,
    ReactiveFormsModule,
    PasswordModule,
    Button,
    ChipsModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})

/**
 * Composant d'inscription utilisateur.
 * - Utilise Reactive Forms pour la validation côté client.
 * - Appelle l'API backend /auth/register avec le locale fourni (fr par défaut).
 * - Affiche un message de succès ou d'erreur (PrimeNG).
 */
export class RegisterComponent {
  /** Formulaire réactif d'inscription */
  readonly form: FormGroup;

  /** Signal d'état de chargement (spinner bouton) */
  readonly loading = signal<boolean>(false);

  /** Signal de succès (texte court) */
  readonly success = signal<string | null>(null);

  /** Signal d'erreur "lisible" pour l'utilisateur */
  readonly error = signal<string | null>(null);

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  constructor() {
    // Construction du formulaire avec validations synchrones basiques
    this.form = this.fb.group({
      firstname: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: [
        '',
        [
          Validators.required,
          Validators.email, // format email
        ],
      ],
      password: [
        '',
        [
          Validators.required,
          Validators.minLength(8), // 8 caractères minimum
        ],
      ],
    });
  }

  /** Raccourcis pratiques pour le template (accès aux controls) */
  get f() {
    return this.form.controls;
  }

  /**
   * Soumission du formulaire d'inscription.
   * - Vérifie la validité
   * - Appelle AuthService.register(...)
   * - Gère les signaux loading / success / error
   */
  submit(): void {
    // On marque tout comme "touché" pour déclencher les messages d'erreur
    this.form.markAllAsTouched();
    this.success.set(null);
    this.error.set(null);

    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);
    const payload = this.form.value; // { firstname, lastname, email, password }

    this.auth.register(payload, 'fr').subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set('Account created. Please check your email to activate your account.');
        this.form.reset();
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.readableError(err));
      },
    });
  }

  /** Transforme les erreurs HTTP en message utilisateur lisible */
  private readableError(err: any): string {
    return (
      err?.error?.error ||
      err?.error?.businessErrorDescription ||
      err?.message ||
      'Unexpected error'
    );
  }
}
