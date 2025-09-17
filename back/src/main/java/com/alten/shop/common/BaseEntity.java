package com.alten.shop.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Classe de base pour toutes les entités persistées.
 *      - Fournit un id technique.
 *      - Gère l'audit (dates et auteurs).
 */

@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)//Listener activé
public class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Version pour l’optimistic locking (détection de mises à jour concurrentes)
    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    //@CreatedBy : Cette annotation est utilisée pour indiquer l'utilisateur qui a créé l'entité.
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    //Cette annotation est utilisée pour indiquer l'utilisateur qui a modifié l'entité
    @LastModifiedBy
    @Column(insertable = false)
    private String lastModifiedBy;

}
