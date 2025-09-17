package com.alten.shop.handler;

import com.alten.shop.exception.*;
import com.alten.shop.code.TokenException;
import com.alten.shop.common.dto.ExceptionResponse;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.alten.shop.handler.BusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Gestionnaire global des exceptions applicatives.
 * <p>
 * Centralise la construction des réponses d'erreurs (format unifié) et
 * mappe les exceptions techniques/métier vers des statuts HTTP cohérents.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Logger SLF4J pour enregistrer les erreurs en interne (fichiers, console, etc.) */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Lorsque le compte utilisateur est verrouillé.
    // Retourne une réponse HTTP 401 (UNAUTHORIZED)
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)  //UNAUTHORIZED (401), ce qui signifie que l'utilisateur n'est pas autorisé à accéder à la ressource demandée.
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    //DisabledException : Levée lorsque le compte utilisateur est désactivé.
    //Réponse : Retourne une réponse HTTP 401 (UNAUTHORIZED)
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_DISABLED.getCode())
                                .businessErrorDescription(ACCOUNT_DISABLED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }



    //BadCredentialsException : Levée pour des informations d'identification incorrectes (mauvais login ou mot de passe).
    //Réponse : Retourne une réponse HTTP 401 (UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException() {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode())
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                                .error("Login and / or Password is incorrect")
                                .build()
                );
    }

    //-----------------------------------------------------------------------------------
    // 403 Forbidden : Token JWT invalide ou expiré
    //-----------------------------------------------------------------------------------
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ExceptionResponse> handleTokenError(
            TokenException ex,
            HttpServletRequest request
    ) {
        // Log un warning pour trace interne
        log.warn("Token error: {}", ex.getMessage());

        // Construction de la réponse d’erreur
        ExceptionResponse body = ExceptionResponse.builder()
                .path(request.getRequestURI())
                .businessErrorCode(TOKEN_INVALID.getCode())
                .businessErrorDescription(TOKEN_INVALID.getDescription())
                .error(ex.getMessage())
                .build();

        return ResponseEntity
                .status(TOKEN_INVALID.getHttpStatus())
                .body(body);

    }

    //Gestion de l'exception UserAlreadyExistsException (409 CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(USER_ALREADY_EXISTS.getHttpStatus()) // ✅ Utilisation du HttpStatus défini dans l'enum
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(USER_ALREADY_EXISTS.getCode()) // ✅ Code unique
                                .businessErrorDescription(USER_ALREADY_EXISTS.getDescription()) // ✅ Description standardisée
                                .error(ex.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(PRODUCT_NOT_FOUND.getHttpStatus())
                .body(ExceptionResponse.builder()
                        .businessErrorCode(PRODUCT_NOT_FOUND.getCode())
                        .businessErrorDescription(PRODUCT_NOT_FOUND.getDescription())
                        .error(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(ProductCodeAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleProductCodeExists(ProductCodeAlreadyExistsException ex) {
        return ResponseEntity.status(PRODUCT_CODE_EXISTS.getHttpStatus())
                .body(ExceptionResponse.builder()
                        .businessErrorCode(PRODUCT_CODE_EXISTS.getCode())
                        .businessErrorDescription(PRODUCT_CODE_EXISTS.getDescription())
                        .error(ex.getMessage())
                        .build());
    }


    /**
     *
     * Gestion d'une tentative d'opération interdite sur un produit
     * (par un utilisateur qui n'est pas admin@admin.com).
     *
     * Gestion des accès interdits sur les produits :
     * - Cas métier : ForbiddenProductOperationException levée dans ProductService
     * - Cas sécurité : AuthorizationDeniedException levée par Spring Security (PreAuthorize)
     */
    @ExceptionHandler({ForbiddenProductOperationException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ExceptionResponse> handleForbiddenProductOperation(Exception ex, HttpServletRequest request) {

        return ResponseEntity
                .status(PRODUCT_OPERATION_FORBIDDEN.getHttpStatus())
                .body(ExceptionResponse.builder()
                        .path(request.getRequestURI())
                        .businessErrorCode(PRODUCT_OPERATION_FORBIDDEN.getCode())
                        .businessErrorDescription(PRODUCT_OPERATION_FORBIDDEN.getDescription())
                        .error(PRODUCT_OPERATION_FORBIDDEN.getDescription())
                        .build());
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ExceptionResponse> handleOptimisticLock(OptimisticLockException ex,
                                                                  HttpServletRequest request) {
        return ResponseEntity
                .status(OPTIMISTIC_LOCK_FAILURE.getHttpStatus())
                .body(ExceptionResponse.builder()
                        .path(request.getRequestURI())
                        .businessErrorCode(OPTIMISTIC_LOCK_FAILURE.getCode())
                        .businessErrorDescription(OPTIMISTIC_LOCK_FAILURE.getDescription())
                        .error(OPTIMISTIC_LOCK_FAILURE.getDescription())
                        .build());
    }

    @ExceptionHandler(InvalidProductStatusException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidStatus(InvalidProductStatusException ex) {
        return ResponseEntity
                .status(PRODUCT_STATUS_INVALID.getHttpStatus())
                .body(ExceptionResponse.builder()
                        .businessErrorCode(PRODUCT_STATUS_INVALID.getCode())
                        .businessErrorDescription(PRODUCT_STATUS_INVALID.getDescription())
                        .error(ex.getMessage())
                        .build());
    }



    //Exception : Gestionnaire général pour toutes les exceptions non spécifiées.
    //Réponse : Retourne une réponse HTTP 500 (INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {
        // Log complet en interne pour debug
        log.error("Unhandled exception", exp);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription("Internal error, please contact the admin")
                                .error(exp.getMessage())
                                .build()
                );
    }
}
