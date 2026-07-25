package in.guvi.event.management.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class EventManagementSystemApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()   // Safe on Render — no .env file needed in production
                .ignoreIfMalformed() // Skip malformed lines without crashing
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        // Extract username and password from DATABASE_URL if provided
        String dbUrl = System.getProperty("DATABASE_URL", System.getenv("DATABASE_URL"));
        if (dbUrl != null && dbUrl.contains("@")) {
            try {
                // Handle both jdbc:postgresql:// and postgres://
                String cleanUri = dbUrl.startsWith("jdbc:") ? dbUrl.substring(5) : dbUrl;
                java.net.URI uri = new java.net.URI(cleanUri);
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    System.setProperty("spring.datasource.username", userInfo[0]);
                    if (userInfo.length > 1) {
                        System.setProperty("spring.datasource.password", userInfo[1]);
                    }
                    // Reconstruct JDBC URL without credentials
                    String newUrl = "jdbc:" + uri.getScheme() + "://" + uri.getHost() +
                            (uri.getPort() != -1 ? ":" + uri.getPort() : "") +
                            uri.getPath() +
                            (uri.getQuery() != null ? "?" + uri.getQuery() : "");
                    System.setProperty("spring.datasource.url", newUrl);
                }
            } catch (Exception e) {
                // Ignore and fallback to original behavior
            }
        }

        SpringApplication.run(EventManagementSystemApplication.class, args);
    }
}
