package com.alten.shop.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService / JwtUtils :  Service qui génère et valide les tokens JWT
 * Classe utilitaire pour la gestion des tokens JWT (JSON Web Tokens).
 * Fournit des méthodes pour :
 * - Générer un token
 * - Extraire des informations depuis un token
 * - Valider un token
 */
@Service
public class JwtService {

    // Clé secrète utilisée pour signer les JWT (récupérée depuis application.yml ou les variables d'env)
    @Value("${application.security.jwt.secret-key}")
    private String jwtSecret;

    // Durée de validité du token (en millisecondes)
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Génère un token JWT pour un utilisateur authentifié.
     *
     * @param userDetails Les détails de l'utilisateur (username, authorities)
     * @return Le token JWT généré
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Crée le token JWT avec les informations fournies.
     *
     * @param claims  Les informations/données à inclure dans le token
     * @param subject le nom d'utilisateur (sera utilisé comme "sub")
     * @return le token JWT signé
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Convertit la clé secrète Base64 en objet Key pour signer ou vérifier un JWT.
     *
     * @return clé de signature HMAC SHA
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Vérifie si un token est valide pour un utilisateur donné.
     *
     * @param token Le token JWT à valider
     * @param userDetails Les détails de l'utilisateur à comparer
     * @return true si le token est valide, false sinon
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extrait le username (subject) d'un token JWT.
     *
     * @param token Le token JWT
     * @return Le username contenu dans le token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Méthode générique pour extraire un claim spécifique d'un token.
     *
     * @param <T> Le type du claim à extraire
     * @param token Le token JWT
     * @param claimsResolver Fonction pour extraire le claim souhaité
     * @return La valeur du claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extactAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse un token JWT et extrait tous ses claims.
     *
     * @param token Le token JWT à parser
     * @return L'objet Claims contenant tous les claims du token
     * @throws JwtException Si le token est invalide ou expiré
     */
    private Claims extactAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Vérifie si le token est expiré.
     *
     * @param token le token JWT
     * @return true si expiré, false sinon
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token JWT.
     *
     * @param token Le token JWT
     * @return La date d'expiration du token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
