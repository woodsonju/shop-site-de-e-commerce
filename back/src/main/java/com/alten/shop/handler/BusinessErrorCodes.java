package com.alten.shop.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

public enum BusinessErrorCodes {

    NO_CODE(0, NOT_IMPLEMENTED, "No code"),
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Current password is incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "The new password does not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "User account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Login and / or Password is incorrect"),
    TOKEN_INVALID(305, FORBIDDEN, "Invalid or expired token"),
    USER_ALREADY_EXISTS(306, CONFLICT, "User with this email already exists"), // ✅ Nouveau code ajouté

    PRODUCT_NOT_FOUND(307, NOT_FOUND, "Product not found"),
    PRODUCT_CODE_EXISTS(308, CONFLICT, "Product code already exists"),
    PRODUCT_OPERATION_FORBIDDEN(309, FORBIDDEN, "Access denied: only admin@admin.com (ADMIN) can perform this operation"),
    PRODUCT_STATUS_INVALID(310, BAD_REQUEST, "Invalid inventory status. Allowed: INSTOCK, LOWSTOCK, OUTOFSTOCK"),


    OPTIMISTIC_LOCK_FAILURE(311, HttpStatus.CONFLICT, "Optimistic lock failure: product was updated by another user");



    @Getter
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final HttpStatus httpStatus;

    BusinessErrorCodes(int code, HttpStatus status, String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = status;
    }
}
