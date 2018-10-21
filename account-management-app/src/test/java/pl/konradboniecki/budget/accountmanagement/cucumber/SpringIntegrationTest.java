package pl.konradboniecki.budget.accountmanagement.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.lazy-initialization=true",
                "spring.main.allow-bean-definition-overriding=true",
                "tests.acceptance.mockFamilyClient=true"
        })
@CucumberContextConfiguration
public class SpringIntegrationTest {
}
