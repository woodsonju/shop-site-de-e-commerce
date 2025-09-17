package com.alten.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration du gestionnaire de tâches asynchrones.
 * Utilise un ThreadPool dédié pour exécuter les méthodes annotées avec @Async.
 * Cette approche est particulièrement utile pour Les envois d'email (afin de ne pas bloquer la réponse HTTP)
 */
@Configuration
@EnableAsync //Activation de la gestion des tâches asynchrones
public class AsyncConfig {

    //création d'un pool de threads qui exécutera les différentes tâches asynchrones
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Nombre minimum de threads toujours disponibles (même si aucune tâche n'est soumise).
        executor.setCorePoolSize(5); //5 threads minimum (On définit la taille du pool à 5 thread)
        // Nombre maximum de threads pouvant être créés si la charge augmente.
        executor.setMaxPoolSize(10);  // 10 threads maximum
        // Nombre maximum de tâches qui peuvent être mises en file d'attente en attendant un thread libre.
        executor.setQueueCapacity(100); // 100 tâches peuvent attendre (On définit la capacité de la file d'attente à 100)
        executor.setThreadNamePrefix("AsyncExecutor-"); //Définit le préfixe des noms des threads.
        executor.initialize();  // initialise le ThreadPoolTaskExecutor.
        return executor;

    }
}
