package pl.konradboniecki.budget.accountmanagement.cucumber.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import pl.konradboniecki.budget.accountmanagement.controller.AccountManagementController;
import pl.konradboniecki.budget.accountmanagement.cucumber.commons.SharedData;
import pl.konradboniecki.budget.accountmanagement.cucumber.security.Security;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@RequiredArgsConstructor
public class CommonSteps {

    final Security security;
    final TestRestTemplate testRestTemplate;
    final SharedData sharedData;

    @After
    public void scenarioCleanup() {
        security.basicAuthentication();

        sharedData.getEmailToAccountIdMap().keySet().stream()
                .filter(Objects::nonNull)
                .forEach(key -> {
                            String accountId = sharedData.getEmailToAccountIdMap().get(key);
                            cleanAccount(accountId);
                        }
                );
        sharedData.clearAfterScenario();
    }


    @Then("the operation is unsuccessful")
    public void theOperationIsUnsuccessful() {
        HttpStatus status = sharedData.getStatusCode();
        assertThat(status.is4xxClientError()).isTrue();
    }

    @Then("the operation is successful")
    public void theOperationIsSuccessful() {
        HttpStatus status = sharedData.getStatusCode();
        assertThat(status.is2xxSuccessful()).isTrue();
    }

    @Then("(.+) is not found$")
    public void accountIsNotFound(String resource) {
        HttpStatus status = sharedData.getStatusCode();
        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Then("(.+) is found$")
    public void accountIsFound(String resource) {
        HttpStatus status = sharedData.getStatusCode();
        assertThat(status).isEqualTo(HttpStatus.OK);
    }

    private void cleanAccount(String accountId) {
        log.info("SCENARIO CLEANUP: Deleting account with id: {}", accountId);
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange(AccountManagementController.BASE_PATH + "/accounts/{accountId}", HttpMethod.DELETE, entity, Void.class, accountId);
        sharedData.setLastResponseEntity(responseEntity);
        Assertions.assertThat(responseEntity.getStatusCode())
                .isIn(HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);
    }

    @Then("I'm redirected to registration form")
    public void iMRedirectedToRegistrationForm() {
        HttpHeaders httpHeaders = sharedData.getLastResponseEntity().getHeaders();

        String redirectLocation = "https://konradboniecki.com.pl/register";
        assertThat(httpHeaders.getLocation()).isNotNull();
        assertThat(httpHeaders.getLocation().toString()).isEqualTo(redirectLocation);
        assertThat(sharedData.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    }

    @Then("I'm redirected to login page")
    public void iMRedirectedToLoginPage() {
        HttpHeaders httpHeaders = sharedData.getLastResponseEntity().getHeaders();

        String redirectLocation = "https://konradboniecki.com.pl/login";
        assertThat(httpHeaders.getLocation()).isNotNull();
        assertThat(httpHeaders.getLocation().toString()).isEqualTo(redirectLocation);
        assertThat(sharedData.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    }
}
