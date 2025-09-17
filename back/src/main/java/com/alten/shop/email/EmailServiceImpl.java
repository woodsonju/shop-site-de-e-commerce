package com.alten.shop.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

/**
 * Service responsable de l'envoi des emails transactionnels et de notifications.
 * Utilise JavaMailSender pour la partie SMTP et Thymeleaf pour les templates HTML.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmailServiceImpl implements EmailService {

    //Interface fournie par Spring pour envoyer des emails.
    private final JavaMailSender mailSender;

    //Composant Thymeleaf qui gère le rendu des templates pour générer du contenu dynamique, comme des emails HTML.
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from; //Adresse email utilisée comme expéditeur

    @Value("${frontapp.url}")
    private String frontAppUrl; // URL du Frontend pour construire les liens dans les emails


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
    //La méthode est asynchrone grâce à l'annotation @Async, ce qui signifie qu'elle s'exécute dans un thread séparé,
    //permettant ainsi à l'application de continuer à fonctionner sans bloquer celui ci lors de l'envoie de l'email
    @Async("taskExecutor") // <--- On précise le nom du Bean Executor
    @Override
    public void sendEmail(String to, String username, EmailTemplateName emailTemplate, String confirmationUrl, String activationCode, String subject) throws MessagingException {

        String templateName = (emailTemplate != null) ? emailTemplate.getName() : "confirm-email";

        // Représente le message email complet.
        MimeMessage mimeMessage = mailSender.createMimeMessage();

         /*
            MimeMessageHelper simplifie la création et l'envoi de messages MIME.
            MULTIPART_MODE_MIXED indique le mode de contenu multipart (généralement utilisé pour les emails
            contenant à la fois du texte et des pièces jointes).
            UTF_8.name() spécifie l'encodage des caractères.
         */
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name());

        //Ces propriétés seront injectées dans le modèle Thymeleaf, permettant de générer un contenu dynamique basé sur ces valeurs.
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        //Le contexte (Context) contient les variables qui seront utilisées pour le rendu du template.
        Context context = new Context();
        context.setVariables(properties);

        //Spécifie l'expéditeur, le destinataire et le sujet de l'email.
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);

          /*
           Utilisé pour générer le contenu final d'un email à partir d'un modèle Thymeleaf.
           Il s'agit d'une instance de SpringTemplateEngine, un composant de Thymeleaf qui est responsable du rendu.

           templateName représente le nom du modèle Thymeleaf à utiliser pour générer le contenu de l'email.
           Ce nom est généralement le nom du fichier de modèle (sans extension) qui se trouve dans le répertoire
           des modèles configuré (ex: src/main/resources/templates).
         */
        //templateName : Le nom du modèle à utiliser pour le rendu.
        //context : Le contexte contenant les variables à intégrer dans le modèle
        String template = templateEngine.process(templateName, context);

        //Définit le contenu de l'email. Le second paramètre true indique que le contenu est du HTML.
        helper.setText(template, true);

        mailSender.send(mimeMessage);


    }

    /**
     * Envoie un e-mail de réinitialisation de mot de passe.
     * Utilise le template "reset-password.html"
     *
     * @param email    Adresse email du destinataire
     * @param jwtToken Jeton JWT à inclure dans le lien de réinitialisation
     * @param locale   Code de langue utilisé pour générer l'URL redirigeant vers le bon segment multilingue
     *                 (ex: "fr" pour français, "en" pour anglais). Ce code est injecté dans l’URL finale comme suit :
     *                 https://frontapp.url/#/{locale}/reset-password?token=xxx
     * @throws MessagingException si l'envoi échoue
     */
    @Override
    public void sendEmailForResetPwd(String email, String jwtToken, String locale) throws MessagingException {
        String confirmationUrl = frontAppUrl + "/#/" + locale + "/reset-password?token=" + jwtToken;
        this.sendEmail(
                email,
                "",
                EmailTemplateName.RESET_PASSWORD,
                confirmationUrl,
                "",  // Pas d'activation code pour reset pwd
                //  logoUrl,
                "Password reset request"
        );
    }
}
