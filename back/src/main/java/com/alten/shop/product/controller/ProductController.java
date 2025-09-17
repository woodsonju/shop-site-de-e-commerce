package com.alten.shop.product.controller;


import com.alten.shop.product.dto.ProductFilter;
import com.alten.shop.product.dto.ProductRequest;
import com.alten.shop.product.dto.ProductResponse;
import com.alten.shop.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des produits.
 * <p>
 * - Les opérations de lecture sont accessibles à tout utilisateur authentifié.
 * - Les opérations d'écriture (create/update/delete) sont restreintes
 * à l'utilisateur <b>admin@admin.com</b> ayant l'autorité <b>ADMIN</b>.
 * </p>
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "CRUD operations for products")
public class ProductController {

    private final ProductService service;

    /**
     * Crée un produit (réservé à admin@admin.com avec rôle ADMIN).
     */
    @Operation(summary = "Create a product (only ADMIN with admin@admin.com)")
    @PreAuthorize("hasAuthority('ADMIN') and authentication.name == 'admin@admin.com'")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    /**
     * Met à jour un produit (réservé à admin@admin.com avec rôle ADMIN).
     */
    @Operation(summary = "Update a product by id (only ADMIN with admin@admin.com)")
    @PreAuthorize("hasAuthority('ADMIN') and authentication.name == 'admin@admin.com'")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Supprime un produit (réservé à admin@admin.com avec rôle ADMIN).
     */
    @Operation(summary = "Delete a product by id (only ADMIN with admin@admin.com)")
    @PreAuthorize("hasAuthority('ADMIN') and authentication.name == 'admin@admin.com'")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère un produit par son identifiant.
     */
    @Operation(summary = "Get a product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }


    /**
     * Liste paginée des produits avec filtres optionnels.
     */
    @Operation(summary = "List products with pagination and optional filters")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        ProductFilter filter = ProductFilter.builder()
                .category(category)
                .q(q)
                .status(status)
                .build();

        return ResponseEntity.ok(service.findAll(filter, pageable));
    }

}
