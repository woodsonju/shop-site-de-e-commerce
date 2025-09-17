package com.alten.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * DTO utilisé pour initier une demande de réinitialisation de mot de passe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ResetPassword", description = "Request to send password reset email")
public class ResetPassword implements Serializable {

    /** Email de l'utilisateur pour recevoir le lien de réinitialisation */
    @Schema(description = "Email of the account to reset password for", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
