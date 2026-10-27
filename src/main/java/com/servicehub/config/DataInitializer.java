package com.servicehub.config;

import com.servicehub.model.AppUser;
import com.servicehub.model.Role;
import com.servicehub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria o primeiro ADMIN (credenciais vêm de variáveis de ambiente) se ele ainda não existir. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UserRepository repository, PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!repository.existsByEmail(adminEmail)) {
            repository.save(new AppUser("Administrador", adminEmail, passwordEncoder.encode(adminPassword), Role.ADMIN));
            log.info("Usuário ADMIN inicial criado: {}", adminEmail);
        }
    }
}
