package com.alten.shop.security.model;

import com.alten.shop.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

/**
 * La classe UserDetails est une interface fournie par Spring Security,
 * qui représente les informations de l'utilisateur pour
 * l'authentification et l'autorisation.
 * Elle fournit des méthodes pour obtenir des détails de l'utilisateur
 * tels que le nom d'utilisateur, le mot de passe et les autorités (rôles).
 *
 * La classe UserSecurity permet à Spring Security d'utiliser
 * l'objet User existant et ses informations (les roles, le password, et le username)
 * pour l'authentification et l'autorisation.
 */
@RequiredArgsConstructor
public class UserSecurity implements UserDetails, Serializable {

    private final User user;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
