package com.alten.shop.auth.mapper;

import com.alten.shop.auth.dto.RegisterRequest;
import com.alten.shop.role.Role;
import com.alten.shop.user.User;
import com.alten.shop.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre {@link RegisterRequest}, {@link User} et {@link UserDto}.
 */
@Component
@RequiredArgsConstructor
public class UserRegistrationMapper {
    /**
     * Transforme un {@link RegisterRequest} en entité {@link User}.
     *
     * @param request     Données d'inscription de l'utilisateur
     * @param defaultRole Rôle par défaut à assigner (ex. USER)
     * @return Entité {@link User} prête à être persistée
     */
    public User toEntity(RegisterRequest request, Role defaultRole) {
        return User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(request.getPassword())
                .enabled(false) // compte désactivé par défaut
                .roles(List.of(defaultRole))
                .build();
    }

    /**
     * Transforme un {@link User} en {@link UserDto} pour l'exposition côté client.
     *
     * @param user entité utilisateur
     * @return DTO contenant les informations publiques de l'utilisateur
     */
    public UserDto toDTO(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .enabled(user.isEnabled())
                .build();
    }

}
