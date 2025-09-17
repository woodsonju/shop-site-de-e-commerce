package com.alten.shop.common.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO standardisé pour représenter une erreur dans les réponses API.
 * Comprend des informations de contexte (timestamp, chemin),
 * un code métier, un message, et éventuellement des détails de validation.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY) // N’inclut que les champs non vides dans le JSON
public class ExceptionResponse {

    /**
     * Un objet LocalDateTime représentant la date et l'heure à laquelle l'erreur s'est produit.
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     *   Une chaîne de caractères représentant le chemin de la requête qui provoqué l'erreur (ex: "/api/auth/register").
     */
    private String path;

    /**
     * Code métier spécifique à l’erreur (ex: 302 pour compte verrouillé).
     */
    private Integer businessErrorCode;

    /**
     * Description métier de l’erreur (ex: "User account is locked").
     */
    private String businessErrorDescription;

    /**
     * Message décrivant l’erreur.
     */
    private String error;

    /**
     * Ensemble des messages de validation (issues de @Valid),
     * utilisé pour renvoyer les détails en cas de 400 Bad Request.
     */
    private Set<String> validationErrors;


}
