package com.alten.shop.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Fournit l'utilisateur actuellement authentifié/connecté dans le contexte d'audit de Spring Data JPA.
 *
 * Cette classe implémente l'interface {@link AuditorAware <String>}, utilisée par Spring Data JPA
 * pour automatiquement remplir les champs annotés avec {@code @CreatedBy} et {@code @LastModifiedBy}.
 *
 * Elle extrait l'identifiant de l'utilisateur connecté à partir du {@link SecurityContextHolder}.
 */
@Component
public class ApplicationAuditAware implements AuditorAware<String> {


    /**
     * Méthode appelée automatiquement par Spring Data JPA
     * pour renseigner/remplir les champs {@code @CreatedBy} et {@code @LastModifiedBy}.
     *
     * @return un {@link Optional} contenant l'identifiant de l'utilisateur connecté
     *         (souvent l'email ou le username), ou system s'il n'est pas authentifié.
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        //Cette ligne récupère l'objet Authentication à partir du contexte de sécurité de Spring.
        // Cet objet contient des informations sur l'utilisateur actuellement authentifié.
        //Cela te permet de connaître l'utilisateur actuellement connecté.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérifie que l'utilisateur est bien authentifié (et non anonyme)
        if(authentication == null ||
                !authentication.isAuthenticated()||
                authentication instanceof AnonymousAuthenticationToken) {
            //   Valeur par défaut : On retourne "system" quand personne n’est
            //   connecté → très utile pour les scripts de démarrage ou les batchs
            return Optional.of("system");
        }
        // Récupère l'utilisateur à partir de l'objet Authentication et retourne
        // son identifiant (userPrincipal.getId()) encapsulé dans un Optional.
        //User userPrincipal = (User) authentication.getPrincipal();
        return Optional.ofNullable(authentication.getName());
    }
}
