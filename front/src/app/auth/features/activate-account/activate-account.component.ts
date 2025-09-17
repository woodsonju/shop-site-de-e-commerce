import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import {AuthService} from "../../auth.service";

/**
 * Composant d'activation de compte.
 * - Saisie d'un code (6 chiffres)
 * - Appel au backend /auth/activate-account?code=...&locale=...
 * - Affiche un message de succès/erreur
 *
 * Bonnes pratiques :
 *  - Utilisation de Signals (Angular 16+) pour l'état local (loading, error, success, etc.)
 *  - Code en anglais, commentaires en français
 *  - Validation basique côté client (6 digits)
 */
@Component({
  selector: 'app-activate-account',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, ButtonModule, MessageModule, ProgressSpinnerModule],
  templateUrl: './activate-account.component.html',
  styleUrls: ['./activate-account.component.scss'],
})
export class ActivateAccountComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Code saisi par l'utilisateur (string pour pouvoir valider caractère par caractère) */
  public code = signal<string>('');
  /** Locale sélectionnée (tu peux la brancher sur l’UI si besoin) */
  public locale = signal<string>('fr');

  /** Indicateur visuel de chargement */
  public loading = signal<boolean>(false);
  /** Message d'erreur lisible par l'utilisateur */
  public error = signal<string | null>(null);
  /** Message de succès lisible par l'utilisateur */
  public success = signal<string | null>(null);
  /** Flag indiquant si une tentative a été effectuée (utilisé pour l’affichage conditionnel) */
  public submitted = signal<boolean>(false);

  /** Code valide : exactement 6 chiffres */
  public codeValid = computed<boolean>(() => this.isNumeric(this.code()) && this.code().length === 6);

  /**
   * Vérifie si la valeur ne contient que des chiffres (0-9).
   * @param value valeur à vérifier
   */
  public isNumeric(value: string | null | undefined): boolean {
    return !!value && /^[0-9]+$/.test(value);
  }

  /**
   * Soumet le code au backend pour activer le compte.
   * - Gère loading/success/error via Signals
   * - Appelle AuthService.confirm(code, locale)
   */
  public submit(): void {
    // Réinitialise les messages
    this.error.set(null);
    this.success.set(null);
    this.submitted.set(false);

    // Validation minimale côté client
    if (!this.codeValid()) {
      this.error.set('Please enter a valid 6-digit code.');
      this.submitted.set(true);
      return;
    }

    const code = this.code();
    const locale = this.locale();

    this.loading.set(true);
    this.confirmAccount(code, locale);
  }

  /**
   * Appelle le service d’authentification pour confirmer l’activation.
   * @param code code d'activation (6 chiffres)
   * @param locale langue (ex: 'fr' ou 'en')
   */
  private confirmAccount(code: string, locale: string): void {
    this.auth.confirm(code, locale).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set('Your account has been successfully activated. You can now log in.');
        this.error.set(null);
        this.submitted.set(true);
      },
      error: (err) => {
        this.loading.set(false);
        this.success.set(null);
        // Message d'erreur générique en anglais (on peut raffiner selon le backend)
        const msg =
          err?.error?.error ||
          err?.error?.businessErrorDescription ||
          err?.message ||
          'Invalid or expired activation code.';
        this.error.set(msg);
        this.submitted.set(true);
      },
    });
  }

  /**
   * Redirige l'utilisateur vers la page de connexion.
   */
  public redirectToLogin(): void {
    this.router.navigateByUrl('/auth/login');
  }

  public onCodeInput(value: string): void {
    this.code.set(value); // on met à jour le Signal manuellement
    if (this.codeValid()) {
      this.submit();
    }
  }
}
