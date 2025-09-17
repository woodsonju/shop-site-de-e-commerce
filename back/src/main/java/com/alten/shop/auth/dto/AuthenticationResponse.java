package com.alten.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO retourné après une authentification réussie.
 * Contient le token JWT et sa durée de validité.
 */
@Getter
@Setter
@Builder
@Schema(name = "AuthenticationResponse", description = "JWT returned after successful authentication")
public class AuthenticationResponse implements Serializable {

    /** Token JWT généré */
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
}

