package pl.konradboniecki.budget.accountmanagement.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.service.AccountRepository;
import pl.konradboniecki.budget.accountmanagement.service.AccountService;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = RANDOM_PORT
)
public class AccountReadByIdTests {

    @Autowired
    private AccountService accountService;
    @Autowired
    private TestRestTemplate rest;
    @MockBean
    private AccountRepository accountRepository;
    @LocalServerPort
    private int port;
    private String baseUrl;
    private HttpEntity<?> httpEntity;
    private static final String ACCOUNT_ID_PRESENT = UUID.randomUUID().toString();

    @BeforeAll
    public void healthcheck() {
        baseUrl = "http://localhost:" + port + AccountManagementController.BASE_PATH;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        httpEntity = new HttpEntity<>(httpHeaders);

        String url = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> response = rest.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("{\"status\":\"UP\"}");
    }

    private void mockAccountWithIdEqualPresentId() {
        Account account = new Account()
                .setId(ACCOUNT_ID_PRESENT)
                .setEmail("test@email.com")
                .setFirstName("firstName")
                .setLastName("lastName")
                .setEnabled(true)
                .setCreated(Instant.now())
                .setFamilyId(UUID.randomUUID().toString());
        when(accountRepository.findById(ACCOUNT_ID_PRESENT))
                .thenReturn(Optional.of(account));
    }

    @Test
    public void givenFindByIdWithDefaultParam_WhenFound_ThenAccountIsReturned() {
        // Given
        String url = baseUrl + "/accounts/" + ACCOUNT_ID_PRESENT;
        mockAccountWithIdEqualPresentId();
        // When
        ResponseEntity<Account> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Account.class);
        // Then
        assertThat(responseEntity.getBody().getId()).isEqualTo(ACCOUNT_ID_PRESENT);
    }

    @Test
    public void givenFindByIdWithIdParam_WhenFound_ThenAccountIsReturned() {
        // Given
        String url = baseUrl + "/accounts/" + ACCOUNT_ID_PRESENT + "?findBy=id";
        mockAccountWithIdEqualPresentId();
        // When
        ResponseEntity<Account> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Account.class);

        // Then
        assertThat(responseEntity.getBody().getId()).isEqualTo(ACCOUNT_ID_PRESENT);
    }

    @Test
    public void givenFindByIdWithDefaultParam_WhenFound_ThenResponseCodeIs200() {
        // Given
        String url = baseUrl + "/accounts/" + ACCOUNT_ID_PRESENT;
        mockAccountWithIdEqualPresentId();
        // When
        ResponseEntity<Account> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Account.class);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenFindByIdWithIdParam_WhenFound_ThenResponseCodeIs200() {
        // Given
        String url = baseUrl + "/accounts/" + ACCOUNT_ID_PRESENT + "?findBy=id";
        mockAccountWithIdEqualPresentId();
        // When
        ResponseEntity<Account> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Account.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void givenFindById_WhenNotFound_ThenResponseCodeIs404() {
        // Given
        String randomAccountId = UUID.randomUUID().toString();
        String url = baseUrl + "/" + randomAccountId;
        when(accountRepository.findById(randomAccountId))
                .thenReturn(Optional.empty());
        // When
        ResponseEntity<Account> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Account.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void givenFindById_WhenInvalidId_ThenResponseCodeIs400() {
        // Given
        String url = baseUrl + "/accounts/345trdsfv";
        // When
        ResponseEntity<Account> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Account.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void givenFindById_WhenUnknownParam_ThenResponseCodeIs400() {
        // Given
        String url = baseUrl + "/accounts/" + ACCOUNT_ID_PRESENT + "?findBy=dupa";
        // When
        ResponseEntity<Account> responseEntity = rest.exchange(url, HttpMethod.GET, httpEntity, Account.class);
        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
//TODO:
//    public void givenFindById_WhenUnknownParam_ThenResponseMessageIsValid(){}
}
