package com.alten.shop.auth.controller;


import com.alten.shop.auth.dto.*;
import com.alten.shop.auth.service.AuthenticationService;
import com.alten.shop.common.dto.StringDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST dédié aux opérations d'authentification et de gestion de compte :
 * <ul>
 *     <li>Inscription d'un utilisateur</li>
 *     <li>Authentification (génération d'un JWT)</li>
 *     <li>Envoi d'email de réinitialisation de mot de passe</li>
 *     <li>Changement de mot de passe via un token</li>
 *     <li>Activation de compte via un code reçu par email</li>
 * </ul>
 * <p>
 * Les méthodes délèguent la logique au service métier {@link AuthenticationService}.
 */
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
/*
    Utilisée pour la documentation API (par exemple avec Swagger), elle décrit ce que ce contrôleur gère.
    Le nom Authentication apparaîtra dans la documentation de l'API.
 */
@Tag(
        name = "Authentication",
        description = "Endpoints for user registration, login, password reset and account activation..."
)
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Authentifie un utilisateur et renvoie un JWT.
     *
     * @param authenticationRequest DTO contenant email et mot de passe
     * @return  réponse contenant le token (JWT) {@link AuthenticationResponse}
     */
    @Operation(
            summary = "User login",
            description = "Authenticate a user with email and password and return a JWT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Bad credentials or account disabled")
    })
    @PostMapping(value = "/authenticate", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest) {
        AuthenticationResponse response =  authenticationService.authenticate(authenticationRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Inscription d'un nouveau User.
     *
     * @param registerRequest DTO d'inscription
     * @param locale  langue du contenu email (ex. "fr", "en")
     * @return 201 Created si tout s'est bien passé
     */
    @Operation(
            summary = "Register new user",
            description = "Create a new User account and send activation email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Registration accepted; check your email"),
            @ApiResponse(responseCode = "400", description = "Validation errors on input")
    })
    @PostMapping(value="/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Void> register(
            @RequestBody @Valid RegisterRequest registerRequest,
            @RequestParam("locale") String locale) throws MessagingException {
        authenticationService.register(registerRequest, locale);
        return  ResponseEntity.accepted().build();
    }

    /**
     * Envoi d'un email de réinitialisation de mot de passe.
     *
     * @param resetPassword DTO contenant l'email de l'utilisateur
     * @param locale        code langue pour le lien
     * @return message de confirmation
     */
    @Operation(
            summary = "Request password reset",
            description = "Send an email with a reset-password link to the given address"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "404", description = "Email not found")
    })
    @PostMapping(value = "/reset-password", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StringDto> resetPassword(
            @RequestBody @Valid ResetPassword resetPassword,
            @RequestParam("locale") String locale) throws MessagingException {
        StringDto result = authenticationService.emailToResetPassword(resetPassword, locale);
        return ResponseEntity.ok(result);
    }


    /**
     * Changement de mot de passe à partir du token reçu par email.
     *
     * @param request DTO contenant le token et le nouveau mot de passe
     * @return message de confirmation
     */
    @Operation(
            summary = "Change password",
            description = "Change the user's password using a valid reset token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token or validation error")
    })
    @PostMapping(value ="/change-password", consumes = "application/json", produces = "application/json")
    public ResponseEntity<StringDto> changePassword(
            @RequestBody @Valid ChangePasswordRequest request) {
        StringDto result = authenticationService.changePassword(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Activation de compte via le code reçu par email.
     *
     * @param code code d'activation
     * @param  locale Code de langue pour l’email de renvoi
     * @return 200 OK si activation réussie
     */
    @Operation(
            summary = "Activate account",
            description = "Activate a newly-registered account using the code sent by email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired activation token")
    })
    @GetMapping("/activate-account")
    public ResponseEntity<Void> activateAccount(
            @RequestParam("code") String code,  @RequestParam("locale") String locale) {
        authenticationService.activateAccount(code, locale);
        return ResponseEntity.ok().build();
    }

}
