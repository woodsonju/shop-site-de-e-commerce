package com.alten.shop.exception;

/**
 * Levée lorsqu'un produit est introuvable.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Product with id [" + id + "] not found");
    }
    public ProductNotFoundException(String code) {
        super("Product with code [" + code + "] not found");
    }
}
