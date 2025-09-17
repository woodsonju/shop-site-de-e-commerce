import {Injectable, inject, signal} from "@angular/core";
import {Product} from "./product.model";
import {HttpClient, HttpParams} from "@angular/common/http";
import {catchError, Observable, of, tap, throwError} from "rxjs";
import {environment} from "../../../environments/environment";
import {ProductPage, ProductQuery} from "./product.types";


/**
 * Service Angular responsable de la communication avec le back-end
 * pour la gestion des produits (CRUD + pagination + filtres).
 *
 * Utilise les Signals Angular pour stocker :
 *  - la liste des produits (contenu de la page courante)
 *  - les métadonnées de pagination (totalElements, totalPages, etc.)
 *  - l'état de chargement et les erreurs éventuelles
 */
@Injectable({
  providedIn: "root"
})
export class ProductsService {

  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/products`;

  //Signals : Capacité à réagir au changement qui peuvent se produire dans l'application

  /** Signal stockant la liste des produits de la page courante */
  private readonly _products = signal<Product[]>([]);

  /** Signal stockant la dernière page reçue */
  private readonly _page = signal<ProductPage | null>(null);

  /** Signal d'état de chargement (utile pour afficher un loader dans le front) */
  private readonly _loading = signal<boolean>(false);

  /** Signal d'erreur (pour afficher un message d'erreur dans le front) */
  private readonly _error = signal<string | null>(null);

  /** Exposition des signals en lecture seule pour éviter les modifications externes */
  public readonly products = this._products.asReadonly();
  public readonly page = this._page.asReadonly();
  public readonly loading = this._loading.asReadonly();
  public readonly error = this._error.asReadonly();



  /*  public get(): Observable<Product[]> {
          return this.http.get<Product[]>(this.path).pipe(
              catchError((error) => {
                  return this.http.get<Product[]>("assets/products.json");
              }),
              tap((products) => this._products.set(products)),
          );
      }*/
  /**
   * Récupère la liste des produits depuis l'API avec pagination et filtres.
   * GET /products?page=&size=&category=&q=&status
   *
   * @returns Un Observable émettant la page de produits reçue
   * @param id
   */
  public getById(id: number): Observable<Product> {
    // Active l'indicateur de chargement (utile pour afficher un loader dans l'UI)
    this._loading.set(true);
    // Active l'indicateur de chargement (utile pour afficher un loader dans l'UI)
    this._error.set(null);

    return this.http.get<Product>(`${this.base}/${id}`).pipe(
      // Lorsque la requête réussit, on désactive le loader
      tap(() => this._loading.set(false)),
      // Gestion des erreurs : si la requête échoue, on capture l'erreur
      catchError((err) => {
        // Stocke un message d'erreur lisible dans le signal _error
        this._error.set(this.readableError(err));
        // Relance l'erreur pour que le composant qui appelle puisse aussi la gérer si nécessaire
        return throwError(() => err);
      })
    );
  }


/*  public create(product: Product): Observable<boolean> {
    return this.http.post<boolean>(this.path, product).pipe(
      catchError(() => {
        return of(true);
      }),
      tap(() => this._products.update(products => [product, ...products])),
    );
  }*/
  /**
   * Crée un nouveau produit dans la base.
   * Accessible uniquement à l'administrateur côté backend.
   *
   * @param payload données du produit à créer.
   * @returns un Observable contenant le produit créé.
   */
  public create(payload: Product): Observable<Product> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.post<Product>(this.base, payload).pipe(
      tap((created) => {
        // Ajoute le produit créé en tête de liste locale.
        this._products.update((list) => [created, ...list]);

        // Incrémente le total si une page existe.
        const p = this._page();
        if (p) this._page.set({ ...p, totalElements: p.totalElements + 1 });
      }),
      catchError((err) => {
        this._error.set(this.readableError(err));
        return throwError(() => err);
      }),
      tap(() => this._loading.set(false))
    );
  }

  /*    public update(product: Product): Observable<boolean> {
          return this.http.patch<boolean>(`${this.path}/${product.id}`, product).pipe(
              catchError(() => {
                  return of(true);
              }),
              tap(() => this._products.update(products => {
                  return products.map(p => p.id === product.id ? product : p)
              })),
          );
      }*/
  /**
   * Met à jour un produit existant.
   *
   * @param id identifiant du produit à mettre à jour.
   * @param payload données mises à jour.
   * @returns un Observable contenant le produit mis à jour.
   */
  public update(id: number, payload: Product): Observable<Product> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.put<Product>(`${this.base}/${id}`, payload).pipe(
      tap((updated) => {
        // Remplace le produit dans la liste locale.
        this._products.update((list) =>
          list.map((p) => (p.id === updated.id ? updated : p))
        );
      }),
      catchError((err) => {
        this._error.set(this.readableError(err));
        return throwError(() => err);
      }),
      tap(() => this._loading.set(false))
    );
  }


  /*    public delete(productId: number): Observable<boolean> {
          return this.http.delete<boolean>(`${this.path}/${productId}`).pipe(
              catchError(() => {
                  return of(true);
              }),
              tap(() => this._products.update(products => products.filter(product => product.id !== productId))),
          );
      }*/
  /**
   * Supprime un produit par son identifiant.
   *
   * @param id identifiant du produit à supprimer.
   */
  public delete(id: number): Observable<void> {
    this._loading.set(true);
    this._error.set(null);

    return this.http.delete<void>(`${this.base}/${id}`).pipe(
      tap(() => {
        // Retire le produit de la liste locale.
        this._products.update((list) => list.filter((p) => p.id !== id));

        // Met à jour le compteur si une page est en cache.
        const p = this._page();
        if (p) this._page.set({...p, totalElements: Math.max(0, p.totalElements - 1)});
      }),
      catchError((err) => {
        this._error.set(this.readableError(err));
        return throwError(() => err);
      }),
      tap(() => this._loading.set(false))
    );
  }

  /**
   * Récupère la liste paginée des produits avec filtres optionnels.
   *
   * @param query objet contenant les paramètres de pagination et de filtrage.
   * @returns un Observable de type ProductPage contenant les produits et les métadonnées.
   */
  public list(query: ProductQuery = {}): Observable<ProductPage> {
    // Active le loader et réinitialise les erreurs.
    this._loading.set(true);
    this._error.set(null);

    // Construit les paramètres HTTP à partir des filtres.
    const params = this.buildParams(query);

    return this.http.get<ProductPage>(this.base, {params}).pipe(
      tap((page) => {
        // Met à jour le cache local avec les produits et les infos de page.
        this._page.set(page);
        this._products.set(page.content ?? []);
      }),
      catchError((err) => {
        // Capture et enregistre l'erreur pour affichage dans l'UI.
        this._error.set(this.readableError(err));
        return throwError(() => err);
      }),
      tap(() => this._loading.set(false))
    );
  }

  /**
   * Extrait un message d'erreur lisible à partir d'une réponse HTTP.
   * Si aucune information spécifique n'est fournie par le backend,
   * un message générique en anglais est renvoyé.
   */
  private readableError(err: any): string {
    return (
      err?.error?.error ||                       // Message d'erreur spécifique renvoyé par ExceptionResponse
      err?.error?.businessErrorDescription ||    // Description fonctionnelle renvoyée par le backend
      err?.message ||                            // Fallback : message d'erreur HTTP brut
      'Unexpected error occurred'                // Fallback final en anglais
    );
  }

  // ---------------------------------------------------------------------
  // Méthodes privées utilitaires
  // ---------------------------------------------------------------------

  /**
   * Construit les paramètres de requête HTTP pour la pagination et le filtrage des produits.
   *
   * Cette méthode prend un objet `ProductQuery` (page, taille, catégorie, recherche, statut)
   * et génère des paramètres GET utilisables dans l'appel API :
   * /products?page=0&size=12&category=...&q=...&status=...
   *
   * @param query objet contenant les paramètres de pagination et de recherche.
   * @returns un objet HttpParams prêt à être passé à HttpClient.
   */
  private buildParams(query: ProductQuery): HttpParams {
    // Initialise les paramètres avec les valeurs par défaut si absentes :
    // page = 0, size = 12
    let params = new HttpParams()
      .set('page', String(query.page ?? 0)) // numéro de page (par défaut 0)
      .set('size', String(query.size ?? 12));// taille de page (par défaut 12)

    // Ajoute la catégorie si elle est définie et non vide
    if (this.hasValue(query.category)) params = params.set('category', String(query.category));

    // Ajoute le mot-clé de recherche si présent (q = query text)
    if (this.hasValue(query.q)) params = params.set('q', String(query.q));

    // Ajoute le statut de stock si présent (INSTOCK, LOWSTOCK, OUTOFSTOCK)
    if (this.hasValue(query.status)) params = params.set('status', String(query.status));

    // Retourne l'objet final contenant les paramètres de requête
    return params;
  }

  /** Vérifie qu'une valeur n'est ni null ni vide. */
  private hasValue(v: unknown): boolean {
    // Vérifie que la valeur n'est ni null, ni undefined, puis supprime les espaces
    // et s'assure que la chaîne résultante n'est pas vide.
    return v !== null && v !== undefined && String(v).trim() !== '';
  }
}
