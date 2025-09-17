import {inject, Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {HttpClient, HttpParams} from "@angular/common/http";
import {TokenService} from "./token.service";
import {Observable} from "rxjs";

/*
  Centralise les appels au backend d’authentification.
    - Stocke le token dans TokenService → réutilisé par l’intercepteur HTTP.
    - Expose des méthodes simples pour les composants :
    - register(...) → inscription.
    - login(...) → authentification et stockage du token.
    - isAuthenticated() → utile dans des guards de route.
    - logout() → supprime le token.
 */
/**
 * Service centralisant les appels d'authentification :
 * - Inscription d'un utilisateur
 * - Connexion (login)
 * - Gestion du token (via TokenService)
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  /** URL de base de l'API d'authentification */
  private readonly base = `${environment.apiUrl}/auth`;

  private readonly http = inject(HttpClient);
  private readonly tokenService = inject(TokenService);


  /**
   * Appelle l'API pour enregistrer un nouvel utilisateur.
   * @param payload Données de l'utilisateur (firstname, lastname, email, password)
   * @param locale Locale (ex: fr, en) utilisée pour les emails d'activation
   * @returns Observable<void> complété lorsque l'inscription est terminée
   */
  register(payload: any, locale: string): Observable<void> {
    return this.http.post<void>(`${this.base}/register?locale=${locale}`, payload);
  }

  /**
   * Authentifie l'utilisateur et stocke le token.
   * @param payload Objet contenant email et password
   * @returns Observable<void> complété après stockage du token
   */
  login(payload: { email: string; password: string }): Observable<void> {
    return new Observable<void>((observer) => {
      this.http.post<{ token: string }>(`${this.base}/authenticate`, payload).subscribe({
        next: (res) => {
          this.tokenService.token = res.token; // ✅ Stockage du token en localStorage
          observer.next();
          observer.complete();
        },
        error: (err) => observer.error(err),
      });
    });
  }

  /**
   * Vérifie si l'utilisateur est connecté (token présent et valide).
   */
  isAuthenticated(): boolean {
    return this.tokenService.isTokenValid();
  }

  /**
   * Déconnecte l'utilisateur (supprime le token du stockage local).
   */
  logout(): void {
    localStorage.removeItem('token');
  }

  /**
   * Envoie le code d'activation au backend pour activer le compte.
   *
   * @param code  Code d'activation (reçu par email)
   * @param locale Code de langue (ex: 'fr', 'en') pour gérer le message de confirmation backend
   * @returns Observable<void> : aucun corps de réponse attendu, succès ou erreur suffisent
   */
  public confirm(code: string, locale: string): Observable<void> {
    const params = new HttpParams().set('code', code).set('locale', locale);
    return this.http.get<void>(`${this.base}/activate-account`, { params });
  }
}
