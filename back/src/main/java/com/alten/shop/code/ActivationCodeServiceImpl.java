package com.alten.shop.code;

import com.alten.shop.code.ActivationCodeService;
import com.alten.shop.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivationCodeServiceImpl implements ActivationCodeService {

    private static final Logger log = LoggerFactory.getLogger(ActivationCodeServiceImpl.class);

    private final ActivationCodeRepository activationCodeRepository;

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
    @Override
    public String generateOrRetrieveActivationCode(User user) {
        Long userId = user.getId();
        if (userId == null) {
            log.warn("User has null ID, cannot generate or retrieve activation code");
            throw new IllegalArgumentException("User must be persisted before generating activation code");
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<ActivationCode> opt = activationCodeRepository.findByUser(user);

        if (opt.isPresent()) {
            ActivationCode existing = opt.get();
            boolean notUsed = existing.getValidatedAt() == null;
            boolean notExpired = existing.getExpiresAt().isAfter(now);
            if (notUsed && notExpired) {
                // Le code existant est toujours valide : on le renvoie
                log.debug("Existing valid activation code for userId={}, returning existing code {}",
                        userId, existing.getCode());
                return existing.getCode();
            }
            // Sinon on régénère : met à jour l'objet existant
            String newCode = generateUniqueCode(6);
            existing.setCode(newCode);
            existing.setCreatedDate(now);
            existing.setExpiresAt(now.plusMinutes(15));
            existing.setValidatedAt(null);
            activationCodeRepository.save(existing);
            log.debug("Updated activation code for userId={}, new code={}, expiresAt={}",
                    userId, newCode, existing.getExpiresAt());
            return newCode;
        } else {
            // Cas inhabituel : pas d'enregistrement existant (idéalement on l'a créé à l'inscription)
            String newCode = generateUniqueCode(6);
            ActivationCode activationCode = createCode(user, newCode);
            activationCodeRepository.save(activationCode);
            log.debug("Created activation code for userId={} at first time, code={}, expiresAt={}",
                    userId, newCode, activationCode.getExpiresAt());
            return newCode;
        }
    }

    /**
     * Vérifie si un code d'activation appartient à l'utilisateur et est encore valide :
     * <ul>
     *   <li>Le code existe et est lié au User.</li>
     *   <li>Il n'a pas encore été consommé (validatedAt == null).</li>
     *   <li>Il n'est pas expiré (expiresAt après la date courante).</li>
     * </ul>
     *
     * @param user L’utilisateur à qui le code devrait appartenir.
     * @param code La chaîne du code reçue en entrée.
     * @return true si toutes les conditions sont remplies, false sinon.
     */
    @Override
    public boolean isCodeValid(User user, String code) {
        return activationCodeRepository.findByCode(code)        //On cherche le code en base
                .filter(c -> c.getUser().equals(user))  //Vérifie que le code appartient bien à cet utilisateur
                .filter(c -> c.getValidatedAt() == null)    //Vérifie que le code n'a pas déjà été utilisé
                .filter(c -> c.getExpiresAt().isAfter(LocalDateTime.now())) //Vérifie que le code n’est pas expiré
                .isPresent(); //Si toutes les conditions sont vraies, retourne true
    }

    /**
     * Marque comme consommé (invalide) le code actif de l'utilisateur.
     * Positionne validatedAt = maintenant afin qu’il ne puisse plus être réutilisé.
     *
     * @param user L’utilisateur dont on invalide le code.
     */
    @Override
    public void invalidateCode(User user) {
        activationCodeRepository.findAllByUser(user).forEach(code -> {
            code.setValidatedAt(LocalDateTime.now());
            activationCodeRepository.save(code);
        });
    }

    /**
     * Recherche un code d'activation par sa valeur unique.
     *
     * @param code La valeur du code recherché.
     * @return Optional contenant l'entité si trouvée, sinon Optional.empty().
     */
    @Override
    public Optional<ActivationCode> findByCode(String code) {
        return activationCodeRepository.findByCode(code);
    }

    /**
     * Persiste ou met à jour un objet ActivationCode.
     * Utile pour mettre à jour des champs comme validatedAt ou expiresAt.
     *
     * @param activationCode L'entité ActivationCode à enregistrer.
     */
    @Override
    public void save(ActivationCode activationCode) {
        activationCodeRepository.save(activationCode);
    }

    /**
     * Boucle pour générer un code unique en base (éviter collision si code unique).
     * @param length
     * @return
     */
    private String generateUniqueCode(int length) {
        String code;
        int maxAttempts = 10;
        int attempt = 0;
        do {
            code = generateCode(length);
            attempt++;
            if (attempt > maxAttempts) {
                log.warn("Unique code generation: max attempts reached, accepting code {}", code);
                break;
            }
        } while (activationCodeRepository.existsByCode(code));
        return code;
    }

    /**
     * Méthode utilitaire privée pour générer une chaîne aléatoire de chiffres de la longueur spécifiée.
     * Utilise {@link SecureRandom} pour la qualité cryptographique.
     *
     * @param length Longueur souhaitée du code numérique (par ex. 6).
     * @return Une chaîne de chiffres aléatoires de la longueur donnée.
     */
    private String generateCode(int length) {
        String digits = "0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // nextInt(digits.length()) fournit un index entre 0 et 9
            sb.append(digits.charAt(random.nextInt(digits.length())));
        }
        return sb.toString();
    }

    /**
     * Méthode utilitaire privée pour créer une instance {@link ActivationCode} pour l’utilisateur donné.
     * On fixe createdDate à maintenant et expiresAt à maintenant + 15 minutes.
     *
     * @param user L'utilisateur lié au code.
     * @param code          La chaîne numérique générée.
     * @return Une nouvelle entité ActivationCode non encore persistée.
     */
    private ActivationCode createCode(User user, String code) {
        LocalDateTime now = LocalDateTime.now();
        return ActivationCode.builder()
                .code(code)
                .createdDate(now)                       // héritée de BaseEntity via @CreatedDate, mais ici on la positionne manuellement si nécessaire
                .user(user)           // association ManyToOne vers BusinessOwner
                .expiresAt(now.plusMinutes(15))         // expiration fixée à 15 minutes
                .build();
    }
}
