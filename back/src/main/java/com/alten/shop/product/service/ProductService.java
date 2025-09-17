package com.alten.shop.product.service;

import com.alten.shop.product.dto.ProductFilter;
import com.alten.shop.product.dto.ProductRequest;
import com.alten.shop.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service métier pour la gestion des produits.
 * <p>
 * Cette interface définit le contrat fonctionnel pour le cycle de vie d'un produit :
 * création, mise à jour, suppression, lecture unitaire et recherche paginée.
 * </p>
 */
public interface ProductService {

    /**
     * Crée un nouveau produit.
     *
     * @param request données de création (code unique, prix, quantité, etc.)
     * @return le produit créé
     */
    ProductResponse create(ProductRequest request);

    /**
     * Met à jour un produit existant.
     *
     * @param id      identifiant technique du produit à mettre à jour
     * @param request nouvelles valeurs (les champs null sont ignorés)
     * @return le produit mis à jour
     */
    ProductResponse update(Long id, ProductRequest request);

    /**
     * Supprime un produit.
     *
     * @param id identifiant technique du produit à supprimer
     */
    void delete(Long id);

    /**
     * Récupère un produit par son identifiant.
     *
     * @param id identifiant technique du produit
     * @return le produit trouvé
     */
    ProductResponse findById(Long id);

    /**
     * Liste paginée des produits avec filtrage facultatif.
     *
     * @param filter   critères de recherche (catégorie, statut d'inventaire, texte libre)
     * @param pageable pagination et tri
     * @return une page de produits
     */
    Page<ProductResponse> findAll(ProductFilter filter, Pageable pageable);
}