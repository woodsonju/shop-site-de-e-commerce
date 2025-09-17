package com.alten.shop.security;

import com.alten.shop.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/*
 * On va Configurer la sécurité de notre  application en utilisant Spring Security
 *
 * @Configuration: L'annotation @Configuration indique que la classe est une classe de configuration.
 * @EnableWebSecurity: L'annotation @EnableWebSecurity est utilisée pour activer la sécurité Web
 * dans une application Spring. Elle doit être appliquée à une classe de configuration
 * et indique à Spring de générer automatiquement une configuration de sécurité Web.
 *
 * @EnableMethodSecurity: L'annotation @EnableMethodSecurity est utilisée pour activer
 * la sécurité au niveau de la méthode dans une application Spring.
 * Elle permet d'utiliser des annotations de sécurité telles que @PreAuthorize, @PostAuthorize, @Secured,
 *  etc., pour sécuriser les méthodes de votre application.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Liste des URLs accessibles sans authentification
     */
    public static String[] authorizedUrls = new String[]{
            "/auth/**",           //Tous les endpoints d’auth (authenticate, register…)
            "/reset-password",    // Réinit. mot de passe : POST /auth/reset-password?locale=…
            "/change-password",   // Changement de mot de passe :  POST /auth/change-password
            "/error",                 // Erreur par défaut Spring


            // Swagger / OpenAPI access (autoriser l'accès sans authentification à la documentation API)
            // Point d’entrée OpenAPI 2 (legacy Swagger 2.0)
            "/v2/api-docs",            // Fournit la documentation JSON pour Swagger 2.0 (rarement utilisé avec Spring Boot 3+)

            // Point d’entrée OpenAPI 3 (nouvelle norme de doc API)
            "/v3/api-docs",            // Endpoint principal pour la doc OpenAPI 3 générée par springdoc-openapi
            "/v3/api-docs/**",         // Sous-routes éventuelles des specs (ex : /v3/api-docs/users)

            // Accès aux ressources Swagger nécessaires pour l'interface graphique
            "/swagger-resources",      // Ressources statiques utilisées par Swagger UI
            "/swagger-resources/**",   // Accès aux dépendances internes comme les configurations JS Swagger

            // Ancienne configuration pour Swagger UI (historiquement utilisée pour Swagger 2)
            "/configuration/ui",       // Configuration de l’interface utilisateur Swagger
            "/configuration/security", // Configuration des aspects sécurité de Swagger UI (ex. auth header)

            // Contient tous les fichiers statiques JS/CSS de Swagger
            "/swagger-ui/**",          // Accès à l’interface Swagger UI en SPA (ex : /swagger-ui/index.html)
            "/swagger-ui/index.html", // accès direct
            "/webjars/**",             // Webjars est utilisé pour embarquer les ressources Swagger (JS/CSS)

            // L'ancienne URL par défaut de Swagger UI (utile pour compatibilité)
            "/swagger-ui.html"         // Point d'entrée direct pour Swagger UI (généralement redirigé vers /swagger-ui/index.html)
    };


  //  private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * Définit l’encodeur de mot de passe utilisé dans l’application (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Service permettant d'aller chercher en bdd l'utilisateur par username
     * UserDetailsService est une interface de Spring Security qui est utilisée pour charger les détails
     * de l'utilisateur à partir de la source de données (par exemple, base de données).
     * On va utiliser cette interface pour implementer la façon dont Spring Security
     * va récuperer et traiter les détails d'authentification de l'utilisateur.
     */
    private final UserDetailsService userDetailsService;

    /**
     *
     * L'AuthenticationManager est l'interface principale de Spring Security pour l'authentification des utilisateurs
     * La méthode utilise la méthode getAuthenticationManager() de l'objet AuthenticationConfiguration
     * pour obtenir l'instance d'AuthenticationManager.
     *
     * En retournant cette instance d'AuthenticationManager, la méthode permet d'injecter
     * l'AuthenticationManager dans d'autres composants de l'application qui en ont besoin,
     * tels que les services d'authentification.
     *
     * Cette configuration est importante car l'AuthenticationManager est utilisé par Spring Security
     * pour effectuer l'authentification des utilisateurs lorsqu'ils tentent de se connecter à l'application.
     *
     * Il peut prendre en charge différents mécanismes d'authentification, tels que l'authentification
     * par nom d'utilisateur et mot de passe, l'authentification par jeton JWT, etc
     *
     *
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * La méthode securityFilterChain est annotée avec @Bean, ce qui indique qu'elle est utilisée
     * pour définir et configurer le filtre de sécurité de Spring Security.
     * Elle prend en paramètre un objet HttpSecurity qui permet de configurer les règles de sécurité.
     *
     * @Bean :
     * Lorsque Spring démarre, il analyse toutes les classes de configuration annotées
     * avec @Configuration (ou équivalentes).
     * Il appelle toutes les méthodes annotées avec @Bean dans ces classes.
     * Le retour de ces méthodes est enregistré dans le contexte de l'application Spring en tant que bean.
     * Ces beans peuvent ensuite être injectés dans d'autres parties de l'application via des annotations
     * comme @Autowired.
     *
     * Dans le cas de SecurityFilterChain, ce bean est utilisé par Spring Security pour sécuriser
     * les requêtes HTTP selon les règles que vous avez définies.
     *
     * csrf(AbstractHttpConfigurer::disable) désactive la protection CSRF (Cross-Site Request Forgery)
     * pour autoriser les requêtes sans utiliser de jeton CSRF
     *
     * @param http  L'objet HttpSecurity utilisé pour configurer les règles de sécurité.
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(withDefaults())   //active CORS avec le bean
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(authorizedUrls)  // Autorise les URLs spécifiées dans authorizedUrls
                        .permitAll()   // Autorise l'accès sans restriction
                        /*
                         * anyRequest().authenticated() configure toutes les autres requêtes pour exiger une authentification.
                         * Cela signifie que l'accès à ces requêtes nécessite une authentification préalable.
                         */
                        .anyRequest()
                        .authenticated() // Exige l'authentification pour toutes les autres requêtes
                )
                //Configure la gestion des sessions pour une politique "sans état" (STATELESS).
                // Cela signifie que Spring Security ne conservera pas d'état de session entre les requêtes,
                // ce qui est typique pour les API REST utilisant des tokens JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(userDetailsService)
                .build();
    }

}
