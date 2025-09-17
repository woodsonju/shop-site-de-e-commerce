import {Product} from "./product.model";

/**
 * Interface représentant une page paginée de produits.
 * Correspond à la réponse de l'API Spring (Page<ProductResponse>).
 */
export interface ProductPage {
  content: Product[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * Représente les paramètres de recherche et pagination
 * à envoyer à l'API GET /products.
 */
export interface ProductQuery {
  /** Page à afficher (par défaut: 0) */
  page?: number;
  /** Taille de page (par défaut: 12) */
  size?: number;
  /** Filtrer par catégorie */
  category?: string | null;
  /** Recherche plein texte (nom, code, description) */
  q?: string | null;
  /** Filtrer par statut d'inventaire */
  status?: 'INSTOCK' | 'LOWSTOCK' | 'OUTOFSTOCK' | null;
}
