import {JwtHelperService} from "@auth0/angular-jwt";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  private static readonly KEY = 'token';

  constructor(private readonly jwtHelper: JwtHelperService) {}

  /** Enregistre le token JWT dans localStorage */
  set token(token: string) {
    localStorage.setItem(TokenService.KEY, token);
  }

  /** Retourne le token JWT courant, ou null s'il n'existe pas */
  get token(): string | null {
    return localStorage.getItem(TokenService.KEY);
  }

  /** Supprime le token (utile lors d'un logout ou d'une 401) */
  clear(): void {
    localStorage.removeItem(TokenService.KEY);
  }

  /**
   * Indique si le token est valide (présent et non expiré).
   * - En cas d'expiration ou d'absence, nettoie le storage et retourne false.
   */
  isTokenValid(): boolean {
    const token = this.token;
    if (!token) {
      return false;
    }
    try {
      const expired = this.jwtHelper.isTokenExpired(token);
      if (expired) {
        this.clear();
        return false;
      }
      return true;
    } catch {
      // Si le token est corrompu/invalide, on nettoie et on invalide
      this.clear();
      return false;
    }
  }

  /** Inverse de isTokenValid, pratique pour des guards */
  isTokenNotValid(): boolean {
    return !this.isTokenValid();
  }

 // constructor() { }

  /*set token(token: string) {
    localStorage.setItem('token', token);
  }

  get token() {
    return localStorage.getItem('token') as string;
  }


  isTokenNotValid() {
    return !this.isTokenValid();
  }

  isTokenValid() {
    const token = this.token;
    if (!token) {
      return false;
    }
    // decode the token
    const jwtHelper = new JwtHelperService();
    // check expiry date
    const isTokenExpired = jwtHelper.isTokenExpired(token);
    if (isTokenExpired) {
      localStorage.clear();
      return false;
    }
    return true;
  }*/
}
