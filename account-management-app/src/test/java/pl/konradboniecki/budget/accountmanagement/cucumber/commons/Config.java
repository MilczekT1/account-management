package pl.konradboniecki.budget.accountmanagement.cucumber.commons;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.konradboniecki.budget.accountmanagement.service.FamilyClient;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.konradboniecki.budget.accountmanagement.cucumber.commons.SharedData.MOCKED_MISSING_FAMILY_ID;
import static pl.konradboniecki.budget.accountmanagement.cucumber.commons.SharedData.MOCKED_PRESENT_FAMILY_ID;

@Slf4j
@Configuration
@EnableConfigurationProperties(AcceptanceTestsProperties.class)
public class Config {

    @Value("${local.server.port:8080}")
    int localServerPort;
    @Autowired
    private AcceptanceTestsProperties acceptanceTestsProperties;

    @Bean
    @Profile("test")
    public TestRestTemplate testRestTemplateOnMavenBuild() {
        log.info("SETUP -> Initializing TestRestTemplate on maven build");
        String rootUri = "http://localhost:" + localServerPort;
        TestRestTemplate testRestTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri(rootUri));
        testRestTemplate.getRestTemplate().getInterceptors().add(new CucumberInterceptor());
        return testRestTemplate;
    }

    @Bean
    @ConditionalOnProperty(value = "tests.acceptance.mockFamilyClient", havingValue = "true")
    FamilyClient familyClient() {
        log.info("Mocking FamilyClient");
        return new FamilyClient(null) {
            @Override
            public boolean isPresentById(String id) {
                switch (id) {
                    case MOCKED_MISSING_FAMILY_ID:
                        return false;
                    case MOCKED_PRESENT_FAMILY_ID:
                        return true;
                    default:
                        throw new IllegalArgumentException("family id not mocked: " + id);
                }
            }
        };
    }

    @Bean
    @Profile("acceptance-tests")
    public TestRestTemplate testRestTemplateOnDeployment() {
        log.info("SETUP -> Initializing TestRestTemplate on deployment");
        String rootUri = acceptanceTestsProperties.getBaseUrl();
        assertThat(rootUri).isNotNull();

        TestRestTemplate testRestTemplate = new TestRestTemplate(new RestTemplateBuilder().rootUri(rootUri));
        testRestTemplate.getRestTemplate().getInterceptors().add(new CucumberInterceptor());
        return testRestTemplate;
    }
}
