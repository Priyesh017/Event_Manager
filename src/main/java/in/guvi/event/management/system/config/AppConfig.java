package in.guvi.event.management.system.config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Bean
    public Resend resendClient() {
        return new Resend(resendApiKey);
    }
}
