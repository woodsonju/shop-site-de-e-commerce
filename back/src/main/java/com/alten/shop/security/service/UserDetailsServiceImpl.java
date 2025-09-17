package com.alten.shop.security.service;

import com.alten.shop.security.model.UserSecurity;
import com.alten.shop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implémentation personnalisée de UserDetailsService pour Spring Security.
 * Elle permet de charger un utilisateur depuis la base de données
 * à partir de son email (username).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Charge l'utilisateur à partir de son email et construit l'objet UserDetails.
     *
     * @param username L'email de l'utilisateur (utilisé comme identifiant)
     * @return L'objet UserDetails de Spring Security
     * @throws UsernameNotFoundException si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailWithRoles(username)
                .map(UserSecurity::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}