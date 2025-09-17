package com.alten.shop.exception;

/**
 * Exception métier levée quand le statut d'inventaire fourni
 * n'appartient pas aux valeurs autorisées.
 */
public class InvalidProductStatusException extends RuntimeException {
    public InvalidProductStatusException(String value) {
        super("Invalid inventory status: '" + value + "'. Allowed values: INSTOCK, LOWSTOCK, OUTOFSTOCK");
    }
}