package com.alten.shop.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Données d’entrée pour créer/mettre à jour un produit.
 * (UI : name, price, description, category)
 * Les validations assurent la cohérence métier (prix positif, quantité ≥ 0, code unique).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload to create/update a product.")
public class ProductRequest {

    @Schema(description = "Optimistic lock version", example = "0")
    //@NotNull(message = "Version is required for update")
    private Long version;

    @NotBlank(message = "Product name is mandatory")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Schema(description = "Product name", example = "Wireless Mouse", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "Description must be 500 characters maximum")
    @Schema(example = "Ergonomic wireless mouse for daily use")
    private String description;

    @NotBlank(message = "Category is mandatory")
    @Schema(example = "Electronics", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @NotNull(message = "Price is mandatory")
    @Positive(message = "Price must be greater than zero")
    @Schema(description = "Unit price (EUR)", example = "29.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

}