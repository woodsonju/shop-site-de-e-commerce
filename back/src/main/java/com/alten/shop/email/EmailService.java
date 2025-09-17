package com.alten.shop.email;

import jakarta.mail.MessagingException;

/**
 * Interface du service de gestion des emails.
 * Fournit les méthodes nécessaires à l'envoi d'e-mails HTML,
 * y compris les e-mails de réinitialisation de mot de passe
 * et d'activation de compte.
 */
public interface EmailService {

    /**
     * Envoie un e-mail HTML personnalisé basé sur un template Thymeleaf.
     *
     * @param to              Adresse email du destinataire
     * @param username        Nom/prénom affiché dans l'email
     * @param emailTemplate   Template Thymeleaf à utiliser
     * @param confirmationUrl Lien (URL) utilisé dans le template (activation ou reset)
     * @param activationCode  Code d'activation (optionnel selon le template)
     * @param subject         Sujet de l'email
     * @throws MessagingException si une erreur survient lors de l'envoi
     */
    void sendEmail(String to,
                   String username,
                   EmailTemplateName emailTemplate,
                   String confirmationUrl,
                   String activationCode,
                   //        String logoUrl,
                   String subject) throws MessagingException;

    /**
     * Envoie un e-mail de réinitialisation de mot de passe.
     * Utilise le template "reset-password.html"
     *
     * @param email    Adresse email du destinataire
     * @param jwtToken Jeton JWT à inclure dans le lien de réinitialisation
     *  @param locale    Code de langue utilisé pour générer l'URL redirigeant vers le bon segment multilingue
     *                   (ex: "fr" pour français, "en" pour anglais). Ce code est injecté dans l’URL finale comme suit :
     *                   https://frontapp.url/#/{locale}/reset-password?token=xxx
     * @throws MessagingException si l'envoi échoue
     */
    void sendEmailForResetPwd(String email, String jwtToken, String locale) throws MessagingException;

}
