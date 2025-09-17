package com.alten.shop.code;

import com.alten.shop.common.BaseEntity;
import com.alten.shop.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActivationCode extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    /**Date d’expiration du code (15 minutes par défaut).*/
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**Date à laquelle le code a été utilisé (ou null sinon).*/
    private LocalDateTime validatedAt;

    //@ManyToOne vers BusinessOwner pour relier le code à un utilisateur
    //L’annotation unique = true sur la colonne de jointure garantit qu’un seul ActivationCode existe
    //par User.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
