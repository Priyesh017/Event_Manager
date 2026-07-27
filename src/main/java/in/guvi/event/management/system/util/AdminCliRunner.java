package in.guvi.event.management.system.util;

import in.guvi.event.management.system.entity.User;
import in.guvi.event.management.system.enums.Role;
import in.guvi.event.management.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminCliRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(String... args) {
        String email = null;
        String password = null;
        boolean runAsCli = false;

        for (String arg : args) {
            if (arg.startsWith("--add-admin=")) {
                String[] parts = arg.substring("--add-admin=".length()).split(":", 2);
                if (parts.length == 2) {
                    email = parts[0];
                    password = parts[1];
                    runAsCli = true;
                }
            }
        }

        if (runAsCli) {
            log.info("=======================================================");
            log.info("🛠️  EventHub Admin CLI");
            log.info("=======================================================");
            
            if (!userRepository.existsByEmail(email)) {
                User admin = User.builder()
                    .name("Custom Admin")
                    .email(email.toLowerCase())
                    .password(passwordEncoder.encode(password))
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();
                userRepository.save(admin);
                log.info("✅ Custom admin account created successfully for: {}", email);
            } else {
                log.info("⚠️  An account already exists for: {}. No changes made.", email);
            }
            
            log.info("=======================================================");
            
            // Shut down the application gracefully since this is a CLI operation
            int exitCode = SpringApplication.exit(context, () -> 0);
            System.exit(exitCode);
        }
    }
}
