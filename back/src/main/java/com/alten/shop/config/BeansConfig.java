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

    /*
        OPTIONS :
        Permet aux clients HTTP d’interroger le serveur pour fournir les options de communications à utiliser
        pour une ressource ciblée ou un ensemble de ressources (Méthodes autorisées, Entête autorisées,
        Origines autorisés, etc..). Utilisé souvent comme requête de pré-vérification Cross-Origin CORS
        (Cross Origin Resource Sharing)

        CORS:
        Cross Origin Ressource Sharing. Avec Angular par exemple avant d’envoyer la méthode post, le navigateur
        envoie d’abord la méthode options pour demander au domaine, s’il a droit de lui envoyer la requête.
        Un navigateur refusera toujours d’envoyer une requête d’un domaine vers un autre ; à moins que
        si le domaine cible autorise cette opération.

        Entêtes autorisés :
        Avant que le navigateur envoie une requête http, il doit d’abord savoir si la requête qui va être
        envoyé par l’application ne contient pas des entêtes qui sont refusés, qui ne sont pas acceptés
        par le serveur.

     */
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

