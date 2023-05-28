package pl.konradboniecki.budget.accountmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.chassis.ChassisApplication;

@EnableJpaRepositories("pl.konradboniecki.budget.accountmanagement.service")
@EnableScheduling
@ChassisApplication
public class AccountServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApp.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
