package com.alten.shop.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO  utilisé pour encapsuler une réponse textuelle.
 * Utile pour renvoyer des messages de confirmation ou d'erreur génériques.
 */
@Getter
@Setter
@Builder
@Schema(name = "StringDto", description = "Generic DTO for returning a simple string result")
public class StringDto implements Serializable {

    /** Contenu texte à afficher côté client */
    @Schema(description = "Result message", example = "Password reset email sent", requiredMode = Schema.RequiredMode.REQUIRED)
    private String result;
}
