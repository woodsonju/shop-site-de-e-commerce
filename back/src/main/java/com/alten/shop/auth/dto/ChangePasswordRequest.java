package com.alten.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * DTO utilisé pour changer le mot de passe de l'utilisateur
 * après vérification du token de réinitialisation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ChangePasswordRequest", description = "New password and reset token")
public class ChangePasswordRequest implements Serializable {

    /** Token/Code reçu par email pour valider la réinitialisation */
    @Schema(description = "JWT token received by email", example = "eyJhbGciOiJIUzI1Ni...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    /** Nouveau mot de passe */
    @Schema(description = "New password", example = "N3wP@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String password;
}
