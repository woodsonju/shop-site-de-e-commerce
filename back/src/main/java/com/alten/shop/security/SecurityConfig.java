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


    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * Définit l’encodeur de mot de passe utilisé dans l’application (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(withDefaults())   //active CORS avec le bean
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(authorizedUrls)
                        .permitAll()   // Autorise l'accès sans restriction
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
