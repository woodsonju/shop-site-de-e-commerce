package com.alten.shop.auth.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

/**
 * DTO utilisé lors de l'inscription d'un nouvel utilisateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "RegisterRequest", description = "All data needed to register a new user.")
public class RegisterRequest implements Serializable {

    @NotEmpty(message = "Firstname is mandatory")
    @NotNull(message = "Firstname is mandatory")
    @Schema(description = "Owner's first name", example = "Lucas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstname;

    @NotEmpty(message = "Lastname is mandatory")
    @NotNull(message = "Lastname is mandatory")
    @Schema(description = "Owner's last name", example = "Moreau", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastname;

    @Email(message = "Contact email  is not well formatted")
    @NotEmpty(message = "Contact email  is mandatory")
    @NotNull(message = "Contact email  is mandatory")
    @Schema(description = "Professional email address", example = "contact@mail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotEmpty(message = "Password is mandatory")
    @NotNull(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    @Schema(description = "Account password", example = "S3cur3P@ss", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
    private String password;


}

