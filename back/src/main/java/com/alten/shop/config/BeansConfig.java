package com.alten.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class BeansConfig {

    @Bean
    public AuditorAware<String> auditAware() {
        return new ApplicationAuditAware();
    }


    @Bean
    public CorsFilter corsFilter() {
        //Création d'une configuration CORS
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        //Autoriser les informations d'identification (cookies, authentifications)
        //Cette méthode permet d'envoyer des cookies ou d'autres informations d'identification avec les requêtes CORS.
        //C'est nécessaire si votre application a besoin de s'authentifier via des cookies ou des jetons envoyés dans les en-têtes.
        config.setAllowCredentials(true);
        //Spécifier les origines autorisées
        //Cette ligne spécifie les origines (domaines) autorisées à faire des requêtes vers votre serveur.
        //Dans cet exemple, seule l'origine http://localhost:4200 est autorisée, ce qui est typique pour une application
        //Angular en développement.
        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        //Spécifier les en-têtes autorisés
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION
        ));
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "DELETE",
                "PUT",
                "PATCH"
        ));
        //Enregistrement de la configuration CORS
        //Enregistre la configuration CORS pour toutes les URL (/**) de l'application.
        //Cela signifie que toutes les requêtes vers n'importe quelle route de l'application devront respecter cette configuration CORS.
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);

    }

}

