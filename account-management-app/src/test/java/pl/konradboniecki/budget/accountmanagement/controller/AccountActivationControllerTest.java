package pl.konradboniecki.budget.accountmanagement.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;
import pl.konradboniecki.budget.accountmanagement.service.*;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;

import java.util.List;
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
class AccountActivationControllerTest {

    @Value("${budget.baseUrl.gateway}")
    private String BASE_URI;

    private String contextPath;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private ActivationService activationService;
    @MockBean
    private AccountService accountService;
    @MockBean
    private ActivationCodeService activationCodeService;
    @MockBean
    private ActivationCodeRepository activationCodeRepository;
    @MockBean
    private AccountRepository accountRepository;

    private HttpEntity<?> httpEntity;

    @BeforeEach
    void setUp() {
        contextPath = "http://localhost:" + port + "/api/account-mgt/v1/accounts";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
        httpEntity = new HttpEntity<>(httpHeaders);
    }

    @Test
    void activateUser_ifAccountDoesNotExist_redirectToRegisterPage() {
        // Given:
        String accountId = UUID.randomUUID().toString();
        String url = contextPath + "/" + accountId + "/activation-codes/" + UUID.randomUUID();
        when(accountService.findById(accountId)).thenReturn(Optional.empty());
        // When:
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        List<String> locationHeader = responseEntity.getHeaders().get("Location");
        assertThat(locationHeader).isNotNull()
                .isNotEmpty();
        assertThat(locationHeader.get(0)).isEqualTo(BASE_URI + "/register");

    }

    @Test
    void activateUser_ifAccountIsAlreadyEnabled_redirectToRegisterPage() {
        // Given:
        String accountId = UUID.randomUUID().toString();
        String url = contextPath + "/" + accountId + "/activation-codes/" + UUID.randomUUID();
        when(accountService.findById(accountId)).thenReturn(Optional.of(new Account().setEnabled(true)));
        // When:
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        List<String> locationHeader = responseEntity.getHeaders().get("Location");
        assertThat(locationHeader).isNotNull()
                .isNotEmpty();
        assertThat(locationHeader.get(0)).isEqualTo(BASE_URI + "/login");
    }

    @Test
    void activateUser_ifAccountIsNotEnabled_redirectToLoginPage() {
        // Given:
        String accId = UUID.randomUUID().toString();
        String activationCodeString = UUID.randomUUID().toString();
        String url = contextPath + "/" + accId + "/activation-codes/" + activationCodeString;
        ActivationCode activationCode = new ActivationCode()
                .setActivationCodeValue(activationCodeString)
                .setAccountId(accId);
        Account account = new Account()
                .setId(accId)
                .setEnabled(true);
        when(accountService.findById(accId)).thenReturn(Optional.of(account));
        when(activationCodeService.findByAccountId(accId)).thenReturn(Optional.of(activationCode));
        // When:
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        List<String> locationHeader = responseEntity.getHeaders().get("Location");
        assertThat(locationHeader).isNotNull()
                .isNotEmpty();
        assertThat(locationHeader.get(0)).isEqualTo(BASE_URI + "/login");
    }

    @Test
    void activateUser_BAHeaderNotRequired() {
        // When:
        String accountId = UUID.randomUUID().toString();
        String activationCode = UUID.randomUUID().toString();
        String url = contextPath + "/" + accountId + "/activation-codes/" + activationCode;
        ResponseEntity<String> responseEntity = testRestTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
        // Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
    }
}

