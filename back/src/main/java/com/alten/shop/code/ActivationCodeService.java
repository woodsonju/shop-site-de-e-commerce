package com.alten.shop.code;

import com.alten.shop.user.User;

import java.util.Optional;

/**
 * Service de gestion des codes d'activation utilisés pour valider et activer
 * les comptes des Utilisateurs.
 *
 * <p>
 * Les responsabilités principales sont :
 * <ul>
 *   <li>Générer ou récupérer un code unique à 6 chiffres pour un utilisateur.</li>
 *   <li>Vérifier la validité d’un code fourni par l’utilisateur.</li>
 *   <li>Consommer (invalider) un code après utilisation.</li>
 *   <li>Mettre à jour ou supprimer le code en base.</li>
 *   <li>Rechercher un code par sa valeur ou par utilisateur.</li>
 * </ul>
 * </p>
 */
public interface ActivationCodeService {

    /**
     * Génère un nouveau code d'activation ou renvoie celui existant si encore valide.
     * <ul>
     *   <li>Si aucun code n’existe pour l’utilisateur, crée un nouvel enregistrement.</li>
     *   <li>Si un code existe et est non expiré/non utilisé, renvoie ce code.</li>
     *   <li>Sinon (expiré ou déjà consommé), remplace l’existant par un nouveau code.</li>
     * </ul>
     *
     * @param user L’utilisateur pour lequel on génère ou récupère le code.
     * @return Le code d'activation (6 chiffres) actif.
     */
    String generateOrRetrieveActivationCode(User user);

    
    /**
     * Vérifie si un code d'activation appartient à l'utilisateur et est encore valide :
     * <ul>
     *   <li>Le code existe et est lié au User.</li>
     *   <li>Il n'a pas encore été consommé (validatedAt == null).</li>
     *   <li>Il n'est pas expiré (expiresAt après la date courante).</li>
     * </ul>
     *
     * @param user L’utilisateur à qui le code devrait appartenir.
     * @param code          La chaîne du code reçue en entrée.
     * @return true si toutes les conditions sont remplies, false sinon.
     */
    boolean isCodeValid(User user, String code);

    /**
     * Marque comme consommé (invalide) le code actif de l'utilisateur.
     * Positionne validatedAt = maintenant afin qu’il ne puisse plus être réutilisé.
     *
     * @param user L’utilisateur dont on invalide le code.
     */
    void invalidateCode(User user);

    /**
     * Recherche un code d'activation par sa valeur unique.
     *
     * @param code La valeur du code recherché.
     * @return Optional contenant l'entité si trouvée, sinon Optional.empty().
     */
    Optional<ActivationCode> findByCode(String code);

    /**
     * Persiste ou met à jour un objet ActivationCode.
     * Utile pour mettre à jour des champs comme validatedAt ou expiresAt.
     *
     * @param activationCode L'entité ActivationCode à enregistrer.
     */
    void save(ActivationCode activationCode);
    
}
