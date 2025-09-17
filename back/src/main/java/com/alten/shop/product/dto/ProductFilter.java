package com.alten.shop.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO de critères de filtrage produits (recherche paginée).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Optional filters to refine product listing.")
public class ProductFilter {

    @Size(max = 50, message = "Category must be 50 characters maximum")
    @Schema(description = "Filter by category (optional)", example = "Peripherals", maxLength = 50)
    private String category;

    //q est un paramètre de recherche libre (free-text search)
    //q applique la recherche sur name, code, description
    @Size(max = 100, message = "Search query must be 100 characters maximum")
    @Schema(description = "Free-text search on code, name or description (optional)", example = "wireless", maxLength = 100)
    private String q;

    @Pattern(regexp = "INSTOCK|LOWSTOCK|OUTOFSTOCK", message = "Status must be one of: INSTOCK, LOWSTOCK, OUTOFSTOCK")
    @Schema(description = "Inventory status filter (optional)", example = "INSTOCK", allowableValues = {"INSTOCK", "LOWSTOCK", "OUTOFSTOCK"})
    private String status;
}