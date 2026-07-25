package in.guvi.event.management.system.util;

import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.Role;
import in.guvi.event.management.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@eventhub.com}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("No app.admin.password specified in environment or config. Skipping default admin seeding.");
            return;
        }

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                .name("System Admin")
                .email(adminEmail.toLowerCase())
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();
            userRepository.save(admin);
            log.info("Default admin account created for: {}", adminEmail);
        } else {
            log.info("Admin account already exists for {}, skipping seed.", adminEmail);
        }
    }
}
