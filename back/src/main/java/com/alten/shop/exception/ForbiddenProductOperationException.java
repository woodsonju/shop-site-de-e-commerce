package com.alten.shop.exception;

/**
 * Exception levée lorsqu'un utilisateur tente de créer, modifier ou supprimer un produit
 * alors qu'il n'a pas les droits nécessaires (pas admin@admin.com ou pas rôle ADMIN).
 */
public class ForbiddenProductOperationException extends RuntimeException {

    public ForbiddenProductOperationException(String requesterEmail) {
        super("Access denied for user " + requesterEmail + ". Only admin@admin.com can perform this operation.");
    }
}