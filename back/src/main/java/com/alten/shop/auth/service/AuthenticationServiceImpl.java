package com.alten.shop.auth.service;

import com.alten.shop.auth.dto.*;
import com.alten.shop.exception.AuthenticateException;
import com.alten.shop.auth.mapper.UserRegistrationMapper;
import com.alten.shop.code.ActivationCode;
import com.alten.shop.code.ActivationCodeService;
import com.alten.shop.code.TokenException;
import com.alten.shop.common.dto.StringDto;
import com.alten.shop.email.EmailService;
import com.alten.shop.email.EmailTemplateName;
import com.alten.shop.exception.UserAlreadyExistsException;
import com.alten.shop.role.Role;
import com.alten.shop.role.RoleRepository;
import com.alten.shop.security.service.JwtService;
import com.alten.shop.user.User;
import com.alten.shop.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Implémentation du service métier pour la gestion de l'authentification.
 * <p>
 * Cette classe gère :
 * - L'enregistrement des utilisateurs
 * - L'authentification et génération de token JWT
 * - L'envoi d'emails pour réinitialiser le mot de passe
 * - Le changement de mot de passe
 * - L'activation de compte via code
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private final EmailService emailService;

    private final UserRegistrationMapper userRegistrationMapper;

    private final PasswordEncoder passwordEncoder;

    private final ActivationCodeService activationCodeService;


    @Value("${frontapp.url}")
    private String frontAppUrl;

    /**
     * Authentifie un utilisateur à partir de son email et mot de passe,
     * puis génère (retourne) un token JWT en cas de succès.
     *
     * @param request Objet contenant les identifiants de connexion (email et mot de passe).
     *                Les informations d'identification de l'utilisateur, encapsulées dans un objet AuthenticationRequest.
     * @return Un objet {@link AuthenticationResponse} contenant le JWT généré.
     *                       Un objet AuthenticationResponse contenant le jeton JWT généré après l'authentification réussie.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 1. Authentification via AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword(), new ArrayList<>())
        );

        // Chargement des détails de l'utilisateur à partir de la base de données en utilisant l'objet UserDetails
        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());

        //Vérification si les détails de l'utilisateur ont été trouvés
        if(user == null) {
            throw new AuthenticateException("User not found");
        }

        // Génération du jeton JWT en utilisant l'objet jwtService
        String token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    /**
     * Enregistre un nouvel utilisateur (BusinessOwner) avec les informations personnelles,
     * professionnelles et l'adresse de l'entreprise.
     * Peut également générer un token d'activation et envoyer un email.
     *
     * @param request Données d'inscription encapsulées dans {@link RegisterRequest}.
     * @param locale  Code de langue utilisé pour générer l'URL
     */
    @Override
    public void register(RegisterRequest request, String locale) throws MessagingException {

        log.info("Registering new user with email: {}", request.getEmail());


        // Vérifie si l'utilisateur existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email already exists");
        }

        // Récupère le rôle USER par défaut
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role USER not found"));

        // Crée l'entité User avec Mapper + PasswordEncoder
        User user = userRegistrationMapper.toEntity(request, userRole);
        //Encoder le mot de passe avant de sauvegarder
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saveUser = userRepository.save(user);

        // Génère un token d'activation
        String activationCode = activationCodeService.generateOrRetrieveActivationCode(saveUser);
        log.info("Activation code generated for userId={}: {}", saveUser.getId(), activationCode);

        // 9. Envoyer l'email d'activation
        sendValidationEmail(saveUser, locale, activationCode);

    }


    /**
     * Envoie un email contenant un lien ou un token permettant à l'utilisateur
     * de réinitialiser son mot de passe.
     *
     * @param resetPassword Objet contenant l'email de l'utilisateur.
     * @param locale        code langue (fr, en, …)
     * @return Une réponse texte encapsulée dans {@link StringDto}.
     */
    @Override
    public StringDto emailToResetPassword(ResetPassword resetPassword, String locale) throws MessagingException {
        //Charger les détails d'un utilisateur grâce à son adresse e-mail
        UserDetails user = userDetailsService.loadUserByUsername(resetPassword.getEmail());

        if (user == null) {
            throw new AuthenticateException("User not found");
        }

        String token = jwtService.generateToken(user);

        String confirmationUrl = frontAppUrl
                + "/#/" + locale
                + "/reset-password?token=" + token;

        emailService.sendEmail(
                user.getUsername(),
                "",
                EmailTemplateName.RESET_PASSWORD,
                confirmationUrl,
                "",
                // logoUrl,
                "Reset your Alten Shop  password"
        );
        return StringDto.builder()
                .result("Password reset email send")
                .build();
    }

    /**
     * Met à jour le mot de passe de l'utilisateur après vérification
     * d'un token/code reçu par email. Le token est validé et expiré ensuite.
     *
     * @param request Objet contenant le nouveau mot de passe et le token de réinitialisation.
     * @return Une réponse texte encapsulée dans {@link StringDto}.
     */
    @Override
    public StringDto changePassword(ChangePasswordRequest request) {
        String email = jwtService.extractUsername(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticateException("User not found"));

        boolean valid = (email.equals(user.getEmail()) && !jwtService.isTokenExpired(request.getToken()));
        if(!valid) {
            throw new TokenException("Invalid token/code");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.saveAndFlush(user);

        return StringDto.builder()
                .result("Password changed successfully")
                .build();
    }

    /**
     * Active un compte utilisateur via un code d'activation.
     *
     * - Si code invalide (not found) : TokenException("Invalid activation code").
     * - Si le compte déjà activé : on retourne sans erreur.
     * - Si code expiré ou déjà utilisé : on met à jour l'entité existante (nouveau code),
     *                                          on envoie l'email avec ce nouveau code,
     *                                          on lève TokenException("Activation code invalid. A new code has been sent.").
     * - Sinon (code valide) : on active le compte, on marque validatedAt=now, on sauvegarde, et on retourne normalement.
     *
     * La transaction est annotée `noRollbackFor = TokenException.class` pour que la mise à jour du code
     * persiste malgré l'exception destinée au front.
     */
    @Override
    @Transactional(noRollbackFor = TokenException.class)
    public void activateAccount(String code, String locale) {


        // Rechercher le code
        ActivationCode activationCode = activationCodeService.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("Received invalid activation code: {}", code);
                    return new TokenException("Invalid activation code");
                });

        User user = activationCode.getUser();
        LocalDateTime now = LocalDateTime.now();


        // Si le compte est déjà activé, on ne fait rien
        if (Boolean.TRUE.equals(user.isEnabled())) {
            log.info("Account already activated for userId={}", user.getId());
            return;
        }

        //Si code expiré ou déjà utilisé, on invalide l'ancien code, et on renvoie un nouveau code par mail
        if (activationCode.getValidatedAt() != null || activationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Activation code {} invalid or already used for userId={}, regenerating",
                    code, user.getId());

            // Générer un nouveau code via mise à jour de l'entité existante
            String newCode = activationCodeService.generateOrRetrieveActivationCode(user);
            log.info("New activation code generated for userId={}: {}", user.getId(), newCode);

            // Envoyer email avec le nouveau code
            try {
                sendValidationEmail(user, locale, newCode);
                log.info("Sent new activation email to {} for userId={}",
                        user.getEmail(), user.getId());
            } catch (MessagingException me) {
                log.error("Failed to send activation email for new code for userId={}", user.getId(), me);
                // Ici la persistence du nouveau code est déjà dans la transaction;
                // on peut logger et renvoyer une exception ou continuer selon besoin.
                throw new TokenException("Activation code expired. Generated new code but email sending failed.", me);
            }
            // Informer le front
            throw new TokenException("Activation code expired. A new code has been sent.");
        }

        // Cas code valide : activation
        user.setEnabled(true);
        userRepository.save(user);

        //Marquer le code comme validé
        activationCode.setValidatedAt(LocalDateTime.now());
        activationCodeService.save(activationCode);

        log.info("Account activated for userId={}, code validated: {}", user.getId(), code);
    }


    /**
     * Envoie un email avec le code d'activation fourni.
     * Ne génère ni ne supprime de code ici.
     *
     * @param user Le destinataire.
     * @param locale        Code de langue pour construire l'URL.
     * @param codeToSend    Le code d'activation à inclure dans l'URL/email.
     */
    private void sendValidationEmail(User user, String locale, String codeToSend) throws MessagingException {

        // Construire l’URL front
        String activationUrl = frontAppUrl
                + "/#/" + locale
                + "/activate-account?code=" + codeToSend + "&locale=" + locale;

        // Envoi de l’email
        emailService.sendEmail(
                user.getEmail(),
                user.getFirstname() + " " + user.getLastname(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                codeToSend,
                //logoUrl,
                "Activate your Alten Shop account"
        );

    }
}
