package com.alten.shop.exception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Levée lorsque le code produit (SKU) existe déjà.
 */
public class ProductCodeAlreadyExistsException extends RuntimeException {
    public ProductCodeAlreadyExistsException(String code) {
        super("Product with code [" + code + "] already exists");
    }
}