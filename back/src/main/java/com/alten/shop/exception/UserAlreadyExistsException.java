package com.alten.shop.exception;

/**
 * Exception levée lorsqu'un utilisateur avec le même email existe déjà.
 * <p>
 * Cette exception est utilisée lors de l'inscription afin d'éviter
 * la création de doublons dans la base de données.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructeur avec message personnalisé.
     *
     * @param email l'email de l'utilisateur qui existe déjà
     */
    public UserAlreadyExistsException(String email) {
        super("User with email [" + email + "] already exists");
    }
}