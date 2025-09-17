package com.alten.shop.product;

import com.alten.shop.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Représente un produit du catalogue e-commerce.
 * Les champs non fournis par l'IHM reçoivent des valeurs par défaut.
 * Règles simples encapsulées ici :
 *  - Défauts à l’insertion (quantity, rating, inventoryStatus)
 *  - Cohérence du statut lors d’une mise à jour
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder        // utiliser SuperBuilder (pas Builder) car BaseEntity l'utilise
@EqualsAndHashCode(callSuper = true)
public class Product extends BaseEntity {

    /** Code produit unique (ex : SKU). */
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** URL ou chemin local d’une image produit. */
    private String image;

    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    private String internalReference;

    /** Identifiant d’emplacement (ex : allée/étagère). */
    private Long shellId;

    /** Statut d’inventaire (affichage front). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus inventoryStatus;

    /** Note moyenne (0..5). */
    @Column(nullable = false)
    private Double rating;

    /** Enum des statuts d’inventaire. */
    public enum InventoryStatus { INSTOCK, LOWSTOCK, OUTOFSTOCK }




    /**
     * Assure des valeurs par défaut cohérentes à l'insertion.
     * - quantity par défaut à 0
     * - rating par défaut à 0.0
     * - inventoryStatus dérivé de quantity
     */
    @PrePersist
    public void prePersistDefaults() {
        if (quantity == null) quantity = 0;
        if (rating == null) rating = 0.0;
        if (inventoryStatus == null) {
            inventoryStatus = (quantity > 0) ? InventoryStatus.INSTOCK : InventoryStatus.OUTOFSTOCK;
        }
    }

    /**
     * Recalcule le statut si la quantité a changé (ou est null).
     * Règle simple : 0 -> OUTOFSTOCK, <10 -> LOWSTOCK, sinon INSTOCK.
     */
    @PreUpdate
    public void preUpdateConsistency() {
        if (quantity == null) quantity = 0;
        if (quantity == 0) {
            inventoryStatus = InventoryStatus.OUTOFSTOCK;
        } else if (quantity < 10) {
            inventoryStatus = InventoryStatus.LOWSTOCK;
        } else {
            inventoryStatus = InventoryStatus.INSTOCK;
        }
    }

}
