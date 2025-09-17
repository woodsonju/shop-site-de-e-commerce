package com.alten.shop.util;

import java.util.Locale;
import java.util.UUID;


/**
 * Utilitaires de génération de références produit.
 * - Centralise la logique pour shellId et internalReference et le code.
 * -
 */
public final class ProductGenerator {

    private ProductGenerator() {}

    public static String generateCode() {
        return "PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Génère un identifiant d’emplacement (shelf/shell) à partir du code produit.
     * <p>
     * Règle simple : hash du code borné à [0..999].
     * Suffisant pour une démo ; peut être remplacé plus tard par une vraie
     * table "Shelf" ou une stratégie métier.
     */
    public static Long generateShellIdFromCode(String productCode) {
        return Math.abs(productCode.hashCode()) % 1000L;
    }

    /**
     * Génère une référence interne lisible.
     * Exemple : INT-AB12CD34
     */
    public static String generateInternalReference() {
        return "INT-REF-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

}