package com.alten.shop.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * DTO représentant les informations publiques d'un utilisateur.
 * <p>
 * Utilisé pour exposer des données utilisateur au front-end,
 * sans révéler les informations sensibles (comme le mot de passe).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Public representation of a User without exposing sensitive data.")
public class UserDto {
    /** Identifiant unique de l'utilisateur */
    @Schema(description = "Unique identifier of the user", example = "42")
    private Long id;

    /** Prénom de l'utilisateur */
    @Schema(description = "User's first name", example = "John")
    private String firstname;

    /** Nom de l'utilisateur */
    @Schema(description = "User's last name", example = "Doe")
    private String lastname;

    /** Adresse email de l'utilisateur */
    @Schema(description = "User's email address", example = "john.doe@company.com")
    private String email;

    /** Rôles associés à cet utilisateur */
    @Schema(description = "Roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles;

    /** Indique si le compte est activé */
    @Schema(description = "Whether the user account is enabled", example = "true")
    private boolean enabled;


}
