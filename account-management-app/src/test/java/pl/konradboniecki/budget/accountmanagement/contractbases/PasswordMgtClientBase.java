package pl.konradboniecki.budget.accountmanagement.contractbases;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.accountmanagement.AccountServiceApp;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.service.AccountRepository;
import pl.konradboniecki.budget.accountmanagement.service.AccountService;

import java.util.Optional;
import java.util.UUID;

import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = AccountServiceApp.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.config.enabled=false"
)
public class PasswordMgtClientBase {

    @Autowired
    protected AccountService accountService;
    @MockBean
    protected AccountRepository accountRepository;

    @LocalServerPort
    int port;

    private static String existingEmail;
    private static String existingAccountId;
    private String notExistingEmail;

    @BeforeEach
    public void setUpMocks() {
        RestAssured.baseURI = "http://localhost:" + this.port;
        RestAssured.config = config().redirect(redirectConfig().followRedirects(false));

        existingAccountId = "d19391f3-66e0-434c-ba2b-01d64cf37a95";
        existingEmail = "existing_email@password-management.com";
        notExistingEmail = "not_existing_email@password-management.com";

        mockExistingAndNotExistingAccount();
        change_password_mock();
    }

    private void mockExistingAndNotExistingAccount() {
        Account account = new Account()
                .setId(existingAccountId)
                .setFamilyId(UUID.randomUUID().toString())
                .setFirstName("testFirstName")
                .setLastName("testLastName")
                .setEmail(existingEmail)
                .setEnabled(true);
        when(accountRepository.findByEmail(existingEmail))
                .thenReturn(Optional.of(account));
        when(accountRepository.findByEmail(notExistingEmail))
                .thenReturn(Optional.empty());
    }

    private void change_password_mock() {
        String missingAccountId = "dbe34030-183e-4118-86db-36eb15fa60b2";
        when(accountRepository.findById(missingAccountId))
                .thenReturn(Optional.empty());

        String existingAccountId = "10d2f132-dc5a-4cfb-b82c-82f2eee80b4e";
        when(accountRepository.findById(existingAccountId))
                .thenReturn(Optional.of(new Account()));
    }
}
