package com.alten.shop.product.service;

import com.alten.shop.auth.service.AuthenticationServiceImpl;
import com.alten.shop.exception.ForbiddenProductOperationException;
import com.alten.shop.exception.InvalidProductStatusException;
import com.alten.shop.exception.ProductCodeAlreadyExistsException;
import com.alten.shop.exception.ProductNotFoundException;
import com.alten.shop.product.Product;
import com.alten.shop.product.ProductRepository;
import com.alten.shop.product.dto.ProductFilter;
import com.alten.shop.product.dto.ProductRequest;
import com.alten.shop.product.dto.ProductResponse;
import com.alten.shop.product.mapper.ProductMapper;
import com.alten.shop.util.ProductGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.alten.shop.product.Product.InventoryStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service produit.
 * <p>Les contrôles d'autorisation "admin@admin.com" sont gérés au niveau contrôleur via @PreAuthorize.</p>
 *
 * <p>Cette classe assure :
 * <ul>
 *   <li>Les règles métier (unicité du code, existence produit)</li>
 *   <li>La journalisation (logs de création, modification, suppression)</li>
 *   <li>Un <b>guard métier</b> pour vérifier que seul <code>admin@admin.com</code> ayant le rôle <b>ADMIN</b>
 *       peut créer, modifier ou supprimer un produit</li>
 * </ul>
 * Les méthodes de lecture (findById / findAll) restent accessibles à tout utilisateur authentifié.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;
    private final ProductMapper mapper;


    /**
     * Récupère l'email de l'utilisateur actuellement connecté depuis le SecurityContext (ou "system" si non authentifié).
     */
    private String currentRequester() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("system");
    }

    /**
     * Guard métier : vérifie que l'utilisateur connecté est autorisé
     * à effectuer des opérations sensibles sur les produits (CUD).
     * Même si nous avons @PreAuthorize dans le contrôleur, on peut rajouter un guard métier
     * dans le service pour être
     * <p>
     * Seul <b>admin@admin.com</b> ayant le rôle <b>ADMIN</b> est autorisé.
     * Si la condition n'est pas remplie, lève {@link ForbiddenProductOperationException}.
     * </p>
     */
    private void checkAdminGuard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null ? auth.getName() : "anonymous");

        boolean isAdmin = auth != null &&
                auth.getAuthorities().stream().anyMatch(a -> "ADMIN".equalsIgnoreCase(a.getAuthority()));

        if (!isAdmin || !"admin@admin.com".equalsIgnoreCase(email)) {
            log.warn("Unauthorized product operation attempt by user={}", email);
            throw new ForbiddenProductOperationException(email);
        }
    }

    private InventoryStatus parseStatus(String value) {
        try {
            return InventoryStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new InvalidProductStatusException(value);
        }
    }

    /**
     * Garantit des valeurs pour shellId et internalReference si absentes.
     * <p>
     * - shellId : dérivé du code produit (borné à 0..999)
     * - internalReference : identifiant interne lisible
     */
    private void ensureReferences(Product entity) {
        if (entity.getShellId() == null) {
            entity.setShellId(ProductGenerator.generateShellIdFromCode(entity.getCode()));
        }
        if (entity.getInternalReference() == null || entity.getInternalReference().isBlank()) {
            entity.setInternalReference(ProductGenerator.generateInternalReference());
        }
    }

    /**
     * Création d’un nouveau produit.
     * - Génère un code unique.
     * - Sauvegarde l'entité en base.
     * - Retourne la réponse DTO.
     *
     * @param request        données de création
     * @return le produit créé
     */
    @Override
    public ProductResponse create(ProductRequest request) {
        checkAdminGuard();

        String requester = currentRequester();
        log.info("Creating product={}  by requester={}", request, requester);

        // MapStruct crée l'entité et génère le code
        Product entity = mapper.toEntity(request);

        // Génération d'un code produit unique
        entity.setCode(ProductGenerator.generateCode());

        // 3) Garantit shellId & internalReference si absents
        ensureReferences(entity);

        Product saved = repository.save(entity);

        // 🔎 Log fonctionnel : utile pour les audits et retours front
        log.info("Product created: id={}, code={}, name={}",
                saved.getId(), saved.getCode(), saved.getName());

        return mapper.toResponse(saved);

    }

    /**
     * Mise à jour d’un produit existant.
     * - Vérifie l’existence en base.
     * - Applique les modifications depuis le DTO.
     * - Sauvegarde l'entité mise à jour.
     *
     * @param id             identifiant technique du produit à mettre à jour
     * @param request        nouvelles valeurs (les champs null sont ignorés)
     * @return le produit mis à jour
     */
    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        checkAdminGuard();
        String requester = currentRequester();

        log.info("Updating product id={} by requester={}", id, requester);

        Product entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: product not found id={}", id);
                    return new ProductNotFoundException(id);
                });

        mapper.updateEntity(entity, request);

        // 3) Garantit shellId & internalReference si absents
        ensureReferences(entity);

        Product saved = repository.save(entity);

        log.info("Product updated: id={}, code={}, name={}",
                saved.getId(), saved.getCode(), saved.getName());

        return mapper.toResponse(saved);

    }

    /**
     * Supprime un produit.
     *
     * @param id             identifiant technique du produit à supprimer
     */
    @Override
    public void delete(Long id) {
        checkAdminGuard();
        String requester = currentRequester();
        log.info("Deleting product id={} by requester={}", id, requester);

        if (!repository.existsById(id)) {
            log.warn("Delete failed: product not found id={}", id);
            throw new ProductNotFoundException(id);
        }

        repository.deleteById(id);
        log.debug("Product deleted id={} by={}", id, requester);
    }

    /**
     * Récupère un produit par son identifiant.
     *
     * @param id identifiant technique du produit
     * @return le produit trouvé
     */
    @Override
    public ProductResponse findById(Long id) {
        log.debug("Fetching product by id={}", id);

        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Fetch failed: product not found id={}", id);
                    return new ProductNotFoundException(id);
                });
    }

    /**
     * Liste paginée des produits avec filtrage facultatif.
     *
     * @param filter   critères de recherche (catégorie, statut d'inventaire, texte libre)
     * @param pageable pagination et tri
     * @return une page de produits
     */
    @Override
    public Page<ProductResponse> findAll(ProductFilter filter, Pageable pageable) {
        log.debug("Listing products with filter={} page={}", filter, pageable);

        Page<Product> page;
        if (filter != null) {
            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                page = repository.findByCategoryIgnoreCase(filter.getCategory(), pageable);
            } else if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                //conversion sûre (400 si invalide)
                InventoryStatus status = parseStatus(filter.getStatus());
                page = repository.findByInventoryStatus(status, pageable);
            } else if (filter.getQ() != null && !filter.getQ().isBlank()) {
                String q = filter.getQ();
                page = repository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q, q, pageable);
            } else {
                page = repository.findAll(pageable);
            }
        } else {
            page = repository.findAll(pageable);
        }

        return page.map(mapper::toResponse);
    }
}
