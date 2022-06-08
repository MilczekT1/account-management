package pl.konradboniecki.budget.accountmanagement.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.accountmanagement.exceptions.AccountNotFoundException;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.service.AccountService;
import pl.konradboniecki.budget.openapi.dto.model.OASPasswordModification;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = RANDOM_PORT
)
class PasswordControllerTest {

    @MockBean
    private AccountService accountService;
    @Autowired
    private TestRestTemplate rest;
    @LocalServerPort
    private int port;
    private String baseUrl;
    private final String validId = UUID.randomUUID().toString();
    private HttpHeaders httpHeaders;

    @BeforeAll
    void healthcheck() {
        baseUrl = String.format("http://localhost:%s%s/accounts", port, PasswordController.BASE_PATH);
        httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());

        String url = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> response = rest.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"status\":\"UP\"}");
    }

    @Test
    void givenChangePassword_WhenAccountFound_ThenResponseCodeIs200() {
        // Given
        when(accountService.findById(validId))
                .thenReturn(Optional.of(new Account()));
        OASPasswordModification passwordModification = new OASPasswordModification()
                .accountId(UUID.fromString(validId))
                .newPassword("abracadabra");
        HttpEntity<OASPasswordModification> entity = new HttpEntity<>(passwordModification, httpHeaders);
        String url = baseUrl + "/" + validId + "/password";
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.PUT, entity, String.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenChangePassword_WhenAccountNotFound_ThenResponseCodeIs404() {
        // Given
        String accountId = UUID.randomUUID().toString();
        OASPasswordModification passwordModification = new OASPasswordModification()
                .accountId(UUID.fromString(accountId))
                .newPassword("abracadabra");
        HttpEntity<OASPasswordModification> entity = new HttpEntity<>(passwordModification, httpHeaders);
        String url = baseUrl + "/" + accountId + "/password";
        doThrow(AccountNotFoundException.class).when(accountService).changePassword(any(), eq(accountId));
        // When
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.PUT, entity, String.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createActivationCode_ifBAHeaderIsMissing_returnUnauthorized() {
        // When:
        String url = baseUrl + "/" + UUID.randomUUID() + "/password";
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(new HttpHeaders()), String.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenMissingAccount_WhenCheckCredentials_ThenResponseCodeIs404() {
        // Given:
        String missingAccountId = UUID.randomUUID().toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("password", Collections.singletonList("passwordHash"));
        httpHeaders.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
        doThrow(AccountNotFoundException.class).when(accountService)
                .throwIfInvalidCredentialsCheck(eq(missingAccountId), any());
        // When:
        String url = String.format("%s/%s/credentials", baseUrl, missingAccountId);
        ResponseEntity<Void> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Void.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void givenMissingBAHeader_WhenCheckCredentials_ThenResponseCodeIs401() {
        // Given:
        String randomAccountId = UUID.randomUUID().toString();
        HttpEntity<?> httpEntity = new HttpEntity<>(new HttpHeaders());
        // When:
        String url = String.format("%s/%s/credentials", baseUrl, randomAccountId);
        ResponseEntity<Void> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Void.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void givenCheckCredentials_WhenPasswordIsCorrect_ThenResponseCodeIs200() {
        // Given:
        String accountId = UUID.randomUUID().toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("password", Collections.singletonList("passwordHash"));
        httpHeaders.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
        doNothing().when(accountService)
                .throwIfInvalidCredentialsCheck(eq(accountId), any());
        // When:
        String url = String.format("%s/%s/credentials", baseUrl, accountId);
        ResponseEntity<Void> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Void.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenCheckCredentials_WhenPasswordIsIncorrect_ThenResponseCodeIs400() {
        // Given:
        String accountId = UUID.randomUUID().toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("password", Collections.singletonList("passwordHash"));
        httpHeaders.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
        Throwable badRequestException = new ResponseStatusException(HttpStatus.BAD_REQUEST);
        doThrow(badRequestException).when(accountService)
                .throwIfInvalidCredentialsCheck(eq(accountId), any());
        // When:
        String url = String.format("%s/%s/credentials", baseUrl, accountId);
        ResponseEntity<Void> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Void.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void givenMissingPasswordHeader_WhenCheckCredentials_ThenResponseCodeIs400() {
        // Given:
        String missingAccountId = UUID.randomUUID().toString();
        HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
        // When:
        String url = String.format("%s/%s/credentials", baseUrl, missingAccountId);
        ResponseEntity<Void> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Void.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
