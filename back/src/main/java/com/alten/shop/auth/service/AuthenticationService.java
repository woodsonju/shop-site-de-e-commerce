package com.alten.shop.auth.service;


import com.alten.shop.auth.dto.*;
import com.alten.shop.common.dto.StringDto;
import jakarta.mail.MessagingException;

/**
 * Service métier pour la gestion de l'authentification.
 */
public interface AuthenticationService {

    /**
     * Authentifie un utilisateur à partir de son email et mot de passe,
     * puis génère un token JWT en cas de succès.
     *
     * @param request Objet contenant les identifiants de connexion (email et mot de passe).
     * @return Un objet {@link AuthenticationResponse} contenant le JWT généré.
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Enregistre un nouvel utilisateur (User) avec les informations personnelles,
     * professionnelles et l'adresse de l'entreprise.
     * Peut également générer un token d'activation et envoyer un email.
     *
     * @param request Données d'inscription encapsulées dans {@link RegisterRequest}.
     * @param locale Code de langue utilisé pour générer l'URL
     */
    void register(RegisterRequest request, String locale) throws MessagingException;

    /**
     * Envoie un email contenant un lien ou un token permettant à l'utilisateur
     * de réinitialiser son mot de passe.
     *
     * @param resetPassword Objet contenant l'email de l'utilisateur.
     * @param locale code langue (fr, en, …)
     * @return Une réponse texte encapsulée dans {@link StringDto}.
     */
    StringDto emailToResetPassword(ResetPassword resetPassword, String locale) throws MessagingException;

    /**
     * Met à jour le mot de passe de l'utilisateur après vérification
     * d'un token reçu par email. Le token est validé et expiré ensuite.
     *
     * @param request Objet contenant le nouveau mot de passe et le token de réinitialisation.
     * @return Une réponse texte encapsulée dans {@link StringDto}.
     */
    StringDto changePassword(ChangePasswordRequest request);

    /**
     * Active un compte utilisateur en utilisant un code envoyé par email
     * lors de l'inscription. Vérifie l'expiration et envoie un nouveau code si besoin.
     *
     * @param code Le code d'activation envoyé à l'utilisateur par email.
     * @param locale Code de langue pour l’email de renvoi
     */
    void activateAccount(String code, String locale);
}
