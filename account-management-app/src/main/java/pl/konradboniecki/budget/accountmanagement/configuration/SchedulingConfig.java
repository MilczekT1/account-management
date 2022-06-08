package pl.konradboniecki.budget.accountmanagement.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.konradboniecki.budget.accountmanagement.service.ActivationCodeCleanupService;
import pl.konradboniecki.budget.accountmanagement.service.ActivationCodeRepository;

@Configuration
public class SchedulingConfig {

    @Bean
    @Profile("operations")
    public ActivationCodeCleanupService dbCleanupService(final ActivationCodeRepository activationCodeRepository) {
        return new ActivationCodeCleanupService(activationCodeRepository);
    }
}
