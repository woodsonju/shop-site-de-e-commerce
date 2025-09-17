package com.alten.shop.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;


/**
 * DTO utilisé pour l'authentification (connexion) de l'utilisateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AuthenticationRequest", description = "Credentials to authenticate a user")
public class AuthenticationRequest implements Serializable {

    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is mandatory")
    @NotNull(message = "Email is mandatory")
    @Schema(description = "User's email address", example = "user@mail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotEmpty(message = "Password is mandatory")
    @NotNull(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    @Schema(description = "User's password", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String password;
}
