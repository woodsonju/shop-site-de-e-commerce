import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpHeaders,
  HttpInterceptor,
  HttpRequest
} from "@angular/common/http";
import {catchError, Observable, throwError} from "rxjs";
import {Injectable} from "@angular/core";
import {TokenService} from "./token.service";
import {environment} from "../../environments/environment";

/**
 * Intercepteur HTTP qui ajoute le header Authorization: Bearer <token>
 * aux requêtes sortantes vers l'API (sauf endpoints publics /auth/**).
 * Gère aussi les erreurs 401 pour nettoyer le token (et optionnellement rediriger).
 */
@Injectable()
export class HttpTokenInterceptor implements HttpInterceptor {
  // Endpoints publics (pas de token)
  private readonly publicPaths = [
    '/auth/register',
    '/auth/authenticate',
    '/auth/activate-account',
    '/auth/reset-password',
    '/auth/change-password',
  ];

  constructor(
    private tokenService: TokenService
  ) {}


  /**
   * Intercepte chaque requête sortante.
   * - Si l'URL cible l'API et n'est pas un endpoint public, on colle le token s'il existe/valide.
   * - On intercepte les 401 pour nettoyer le token.
   */
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const isApiCall = request.url.startsWith(environment.apiUrl);
    const isPublic = this.publicPaths.some((p) => request.url.includes(p));

    let req = request;

    // Ajout conditionnel du token
    if (isApiCall && !isPublic) {
      const token = this.tokenService.token;
      if (token && this.tokenService.isTokenValid()) {
        req = request.clone({
          headers: new HttpHeaders({
            Authorization: `Bearer ${token}`,
          }),
        });
      }
    }

    // Gestion des erreurs 401 (token expiré/invalide)
    return next.handle(req).pipe(
      catchError((err: unknown) => {
        if (err instanceof HttpErrorResponse && err.status === 401) {
          this.tokenService.clear();
          // Optionnel : redirection vers /login si tu as un écran d'auth
          // this.router.navigate(['/login']);
        }
        return throwError(() => err);
      })
    );

  }

 /* //Cet intercepteur ajoute un jeton d'authentification (Bearer token) à chaque requête HTTP
  //next est un HttpHandler qui est utilisé pour transmettre la requête au prochain intercepteur dans la chaîne
  //ou pour envoyer la requête au backend si aucun autre intercepteur n'est présent.
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token: string = this.tokenService.token;
    if (token) {
      //Clone la requete en mettant à jour le header Authorisation
      //Clone la requête  et ajoute un en-tête Authorization contenant le token sous forme de Bearer token.
      const authReq = request.clone({
        headers: new HttpHeaders({
          Authorization: `Bearer ${token}`
        })
      });
      //Passe la requête clonée (avec le jeton d'authentification) au prochain intercepteur ou l'envoie au serveur.
      return next.handle(authReq);
    }
    //Si aucun token n'est disponible, la requête  est envoyée sans modification.
    return next.handle(request);
  }*/
}
