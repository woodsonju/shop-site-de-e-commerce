package com.alten.shop.code;

import com.alten.shop.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivationCodeRepository extends JpaRepository<ActivationCode, Long> {

    /**
     * Recherche un code d'activation par sa valeur.
     *
     * @param code le code d'activation
     * @return un Optional contenant le code s’il existe
     */
    Optional<ActivationCode> findByCode(String code);

    /**
     * Vérifie si un code existe déjà pour éviter les doublons lors de sa génération.
     *
     * @param code le code à tester
     * @return true s’il existe déjà
     */
    boolean existsByCode(String code);



    /**
     * Récupère tous les codes d'activation associés à un utilisateur.
     *
     * @param user l'utilisateur concerné
     * @return liste des codes liés à l'utilisateur
     */
    List<ActivationCode> findAllByUser(User user);


    /**
     * Récupérer l’unique ActivationCode pour un user
     */
    Optional<ActivationCode> findByUser(User user);


}
