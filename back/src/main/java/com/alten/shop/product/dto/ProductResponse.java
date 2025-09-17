package com.alten.shop.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Réponse exposée au front pour un produit.
 * <p>Inclut les champs d'audit.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Public representation of a product.")
public class ProductResponse {

    @Schema(description = "Optimistic lock version", example = "0")
    private Long version;

    @Schema(description = "Technical identifier", example = "101")
    private Long id;

    @Schema(description = "Unique product code (SKU)", example = "AL-PRD-001")
    private String code;

    @Schema(description = "Product name", example = "Wireless Mouse")
    private String name;

    @Schema(description = "Product description", example = "Ergonomic wireless mouse with 2.4 GHz connection")
    private String description;

    @Schema(description = "Image URL", example = "https://cdn.example.com/img/mouse.png")
    private String image;

    @Schema(description = "Product category", example = "Peripherals")
    private String category;

    @Schema(description = "Unit price (EUR)", example = "29.9")
    private Double price;

    @Schema(description = "Available quantity", example = "120")
    private Integer quantity;

    @Schema(description = "Internal reference", example = "INT-REF-9932")
    private String internalReference;

    @Schema(description = "Shelf identifier", example = "42")
    private Long shellId;

    @Schema(description = "Inventory status", example = "INSTOCK", allowableValues = {"INSTOCK", "LOWSTOCK", "OUTOFSTOCK"})
    private String inventoryStatus;

    @Schema(description = "Average rating (0..5)", example = "4.5")
    private Double rating;


    /** Dates d'audit mappées depuis BaseEntity. */
    @Schema(description = "Creation timestamp (audit)", example = "2025-09-14T10:23:45")
    private LocalDateTime createdAt;
    @Schema(description = "Last update timestamp (audit)", example = "2025-09-14T11:10:02")
    private LocalDateTime updatedAt;
}