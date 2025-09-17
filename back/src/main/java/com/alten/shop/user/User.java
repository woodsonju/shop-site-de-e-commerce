package com.alten.shop.user;

import com.alten.shop.common.BaseEntity;
import com.alten.shop.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends BaseEntity {


    @Column(nullable = false) private String firstname;
    @Column(nullable = false) private String lastname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    //Représente l'état d'activation de l'utilisateur.
    private boolean enabled;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    public String getFullName() { return firstname + " " + lastname; }

}
