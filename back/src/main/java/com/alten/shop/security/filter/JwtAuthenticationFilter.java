package com.alten.shop.security.filter;

import com.alten.shop.common.dto.ExceptionResponse;
import com.alten.shop.security.SecurityConfig;
import com.alten.shop.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.alten.shop.handler.BusinessErrorCodes.TOKEN_INVALID;

/**
 * Filtre personnalisé pour intercepter et valider les tokens JWT dans les requêtes HTTP.
 *  Filtre Spring Security exécuté une seule fois par requête HTTP.
 *  Son rôle est de :
 *      - Intercepter l'en-tête Authorization
 *      - Extraire et valider un JWT
 *      - Authentifier l'utilisateur dans le contexte Spring Security si le token est valide
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        //Skip CORS
        //Gestion ses requêtes OPTIONS — essentiel pour les clients Angular ou React
        //Le navigateur envoie d’abord une requête OPTIONS pour vérifier les permissions.
        //Si tu la bloques, le client JavaScript (ex: Angular) ne pourra jamais faire ses appels GET/POST.
        //La requête avec OPTIONS ne sera pas acceptée car OPTIONS ne contient pas le Token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        //Gestion du servletPath en excluant les routes publiques définies dans SecurityConfig.authorizedUrls.
        // 🔍 Récupère le chemin relatif sans context-path (/api/v1)
        String fullPath = request.getRequestURI(); // ex: /api/v1/swagger-ui/index.html
        String contextPath = request.getContextPath(); // ex: /api/v1
        String servletPath = fullPath.substring(contextPath.length()); // ex: /swagger-ui/index.html
        log.debug("JwtFilter checking path = {}", servletPath);

        // Si l’URL est dans la liste  (Swagger, Auth, etc.), on skippe le filtre
        for (String pattern : SecurityConfig.authorizedUrls) {
            if (pathMatcher.match(pattern, servletPath)) {
                log.debug("Path '{}' matches whitelist '{}', skipping JWT", servletPath, pattern);
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 2) On récupère l'en-tête Authorization (peut être null ou mal formé)
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            String jwtToken = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(jwtToken);

                // 3) Si l'utilisateur n'est pas déjà dans le contexte et le token est valide
                //Vérifie si le nom d'utilisateur n'est pas nul
                //et s'il n'est pas déjà authentifié : cela signifie que le filtre n'essaie pas de réauthentifier
                //un utilisateur déjà authentifié dans le contexte de sécurité.
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    //Charge les détails de l'utilisateur à partir du service userDetailsService
                    //en utilisant le nom d'utilisateur.
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    //Valide le jeton d'authentification en utilisant la méthode validateToken de l'objet jwtUtils.
                    if (jwtService.isTokenValid(jwtToken, userDetails)) {
                        // Crée une authentification basée sur le jeton JWT et l'associe au contexte de sécurité :
                        //Si le token est valide, un UsernamePasswordAuthenticationToken est créé avec les détails
                        //de l'utilisateur et ses rôles (Authorities), pour représenter l'utilisateur authentifié.
                        //l'utilisateur comme authentifié pour cette requête.
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                        // Ajout des détails de l'authentification
                        // Cela inclut les détails de la requête HTTP comme l'adresse IP, etc.
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        // Le contexte de sécurité (SecurityContextHolder) est mis à jour
                        // avec cette authentification, permettant ainsi à Spring Security de considérer
                        //l'utilisateur comme authentifié pour cette requête.
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("JWT valid for user: {}", username);
                    }
                }
            } catch (ExpiredJwtException e) {
                log.warn("JWT expired: {}", e.getMessage());
                sendUnauthorized(response, request, "Token expired");
                return;
            } catch (Exception e) {
                log.warn("JWT invalid: {}", e.getMessage());
                sendUnauthorized(response, request, "Invalid token");
                return;
            }
        }else {
            log.debug("No Authorization header found, skipping JWT validation.");
        }
        // 4) On continue la chaîne : Spring Security décidera ensuite si l'accès est autorisé
        //filterChain.doFilter(request, response) permet de poursuivre
        //le traitement de la requête en appelant le filtre suivant dans la chaîne
        //Si aucun autre filtre n'est présent dans la chaîne,
        //la requête est finalement transmise à la servlet ou à la ressource demandée.
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response,
                                  HttpServletRequest request,
                                  String message) throws IOException {
        // 1) Construire le corps de réponse unifié
        ExceptionResponse body = ExceptionResponse.builder()
                .timestamp(LocalDateTime.now())                  // when
                .path(request.getRequestURI())                   // which endpoint
                .businessErrorCode(TOKEN_INVALID.getCode())                          // votre code métier (305)
                .businessErrorDescription(TOKEN_INVALID.getDescription())        // description générique
                .error(message)                                  // détail spécifique (e.g. "Token expired")
                .build();

        // 2) Sérialiser et renvoyer
        response.setStatus(TOKEN_INVALID.getHttpStatus().value()); //(403) : le vrai statut HTTP renvoyé
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);

        // 3) Log interne (stacktrace) pour audit, sans l’exposer au client
        log.debug("Unauthorized access to {}: {}", request.getRequestURI(), message);
    }
}
