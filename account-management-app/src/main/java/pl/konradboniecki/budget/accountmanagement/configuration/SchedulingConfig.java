package pl.konradboniecki.budget.accountmanagement.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.konradboniecki.budget.accountmanagement.service.ActivationCodeCleanupService;
import pl.konradboniecki.budget.accountmanagement.service.ActivationCodeRepository;

@Configuration
public class SchedulingConfig {
    @Autowired
    private ActivationCodeRepository activationCodeRepository;

    @Bean
    @Profile("operations")
    public ActivationCodeCleanupService dbCleanupService() {
        return new ActivationCodeCleanupService(activationCodeRepository);
    }
}
