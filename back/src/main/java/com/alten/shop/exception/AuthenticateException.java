package com.alten.shop.exception;

public class AuthenticateException extends RuntimeException {

    /**
     * Exception levée lors d'une erreur d'authentification.
     * Hérite de RuntimeException pour provoquer automatiquement un rollback.
     */
    public AuthenticateException() {
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public AuthenticateException(String message) {
        super(message);
    }

}

