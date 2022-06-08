package pl.konradboniecki.budget.accountmanagement.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.accountmanagement.exceptions.AccountNotFoundException;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        properties = "spring.cloud.config.enabled=false"
)
class ActivationServiceTest {

    @Autowired
    private ActivationService activationService;
    @MockBean
    private ActivationCodeService activationCodeService;
    @MockBean
    private AccountService accountService;

    private String notExistingId;
    private String idOfExistingAndEnabledAccount;
    private String idOfExistingAndNotEnabledAccount;
    private String invalidActivationCode;
    private String validActivationCode;
    @Value("${budget.baseUrl.gateway}")
    private String siteBaseUrl;

    @BeforeAll
    void setUp() {
        notExistingId = UUID.randomUUID().toString();
        idOfExistingAndEnabledAccount = UUID.randomUUID().toString();
        idOfExistingAndNotEnabledAccount = UUID.randomUUID().toString();
        invalidActivationCode = "randomString";
        validActivationCode = UUID.randomUUID().toString();
    }

    @Test
    void redirectToRegistrationPageWhenAccountNotFound() {
        // Given:
        when(accountService.findById(notExistingId)).thenReturn(Optional.empty());
        // When:
        String location = activationService.activateAccount(notExistingId, invalidActivationCode);
        // Then:
        assertThat(location).isEqualTo(siteBaseUrl + "/register");
    }

    @Test
    void redirectToLoginPageWhenAccountIsNotEnabledYet() {
        // Given:
        mockReturningPresentAndEnabledAccount();
        // When:
        String location = activationService.activateAccount(idOfExistingAndEnabledAccount, invalidActivationCode);
        // Then:
        assertThat(location).isEqualTo(siteBaseUrl + "/login");
    }

    @Test
    void throwExceptionWhenActivationCodeIsNotPresent() {
        // Given:
        mockReturningPresentAndNotEnabledAccount();
        when(activationCodeService.findByAccountId(idOfExistingAndNotEnabledAccount))
                .thenReturn(Optional.empty());
        // When:
        ResponseStatusException throwable = catchThrowableOfType(() -> activationService.activateAccount(idOfExistingAndNotEnabledAccount, invalidActivationCode),
                ResponseStatusException.class);
        // Then:
        Assertions.assertAll(
                () -> assertThat(throwable).isNotNull(),
                () -> assertThat(throwable).isInstanceOf(ResponseStatusException.class),
                () -> assertThat(throwable.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST)
        );
    }

    @Test
    void throwExceptionWhenActivationCodeDoesNotMatch() {
        // Given:
        mockReturningPresentAndNotEnabledAccount();
        mockReturningActivationCode();
        // When:
        ResponseStatusException throwable = catchThrowableOfType(() -> activationService.activateAccount(idOfExistingAndNotEnabledAccount, invalidActivationCode),
                ResponseStatusException.class);
        // Then:
        Assertions.assertAll(
                () -> assertThat(throwable).isNotNull(),
                () -> assertThat(throwable).isInstanceOf(ResponseStatusException.class),
                () -> assertThat(throwable.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST)
        );
    }

    @Test
    void activateUserAndRedirectToLoginPage() {
        // Given:
        mockReturningPresentAndNotEnabledAccount();
        mockReturningActivationCode();
        // When:
        String location = activationService.activateAccount(idOfExistingAndNotEnabledAccount, validActivationCode);
        // Then:
        assertThat(location).isEqualTo(siteBaseUrl + "/login");
    }

    @Test
    void throwAccountNotFoundExceptionIfNotFound() {
        // Given:
        String idOfMissingAccount = UUID.randomUUID().toString();
        when(accountService.findById(idOfMissingAccount))
                .thenReturn(Optional.empty());
        // When:
        Throwable throwable = catchThrowableOfType(() -> activationService.createActivationCodeForAccountWithId(idOfMissingAccount), RuntimeException.class);
        // Then:
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(AccountNotFoundException.class);
    }


    private void mockReturningPresentAndEnabledAccount() {
        Account account = new Account()
                .setId(idOfExistingAndEnabledAccount)
                .setEnabled(true);
        when(accountService.findById(idOfExistingAndEnabledAccount)).thenReturn(Optional.of(account));
    }

    private void mockReturningPresentAndNotEnabledAccount() {
        Account account = new Account()
                .setId(idOfExistingAndNotEnabledAccount)
                .setEnabled(false);
        when(accountService.findById(idOfExistingAndNotEnabledAccount)).thenReturn(Optional.of(account));
    }

    private void mockReturningActivationCode() {
        ActivationCode activationCode = new ActivationCode()
                .setAccountId(idOfExistingAndNotEnabledAccount)
                .setActivationCode(validActivationCode);
        when(activationCodeService.findByAccountId(idOfExistingAndNotEnabledAccount))
                .thenReturn(Optional.of(activationCode));
    }
}
