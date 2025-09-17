package com.alten.shop.email;


import lombok.Getter;

/**EmailTemplateName Enum doit contenir tous les noms de templates que tu utilises.*/
@Getter
public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("emails/activate_account"), //Après inscription, pour activer le compte
    RESET_PASSWORD("emails/reset-password");  //Quand un utilisateur clique sur "Mot de passe oublié"

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}

