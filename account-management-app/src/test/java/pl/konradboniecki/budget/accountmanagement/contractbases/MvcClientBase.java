package pl.konradboniecki.budget.accountmanagement.contractbases;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.accountmanagement.AccountServiceApp;
import pl.konradboniecki.budget.accountmanagement.controller.PasswordController;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;
import pl.konradboniecki.budget.accountmanagement.service.AccountRepository;
import pl.konradboniecki.budget.accountmanagement.service.AccountService;
import pl.konradboniecki.budget.accountmanagement.service.ActivationCodeService;
import pl.konradboniecki.budget.accountmanagement.service.FamilyClient;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.config;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = AccountServiceApp.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.config.enabled=false"
)
public class MvcClientBase {

    @Autowired
    protected PasswordController passwordController;
    @Autowired
    protected AccountService accountService;
    @MockBean
    protected AccountRepository accountRepository;
    @MockBean
    protected ActivationCodeService activationCodeService;
    @MockBean
    protected FamilyClient familyClient;

    private static String existingEmail;
    private String notExistingEmail;
    private static String existingEmailInFindByMailContracts;

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUpMocks() {
        RestAssured.baseURI = "http://localhost:" + this.port;
        RestAssured.config = config().redirect(redirectConfig().followRedirects(false));
        findByIdContracts_mockExistingAndNotExistingAccount();

        existingEmail = "existing_email@mail.com";
        existingEmailInFindByMailContracts = "existing_email@find-by-mail.com";
        notExistingEmail = "not_existing_email@mail.com";

        findByEmailContracts_mockExistingAndNotExistingAccount();

        saveAccountContracts_mockSuccessfulCreationOfAnAccount();
        saveAccountContracts_mockConflictDuringCreationOfAnAccount();

        activateUserContracts__mockAccounts();
        activateUserContracts_mockActivationCodeToPersist();

        activateUserContracts_mockNotExistingAccount();
        credentialsContracts_mockCredentialChecks();

        familyAssignmentContracts_mockFamilyAssignments();
    }

    private void findByIdContracts_mockExistingAndNotExistingAccount() {
        String existingAccountId = "178a715a-4d54-403d-b559-def430dd8a5b";
        String missingAccountId = "cc2871a2-e6b1-4490-8840-9d50502074b0";
        Account account = new Account()
                .setId(existingAccountId)
                .setFamilyId(UUID.randomUUID().toString())
                .setFirstName("testFirstName")
                .setLastName("testLastName")
                .setEmail(existingEmail)
                .setEnabled(true);
        when((accountRepository.findById(existingAccountId)))
                .thenReturn(Optional.of(account));
        when((accountRepository.findById(missingAccountId)))
                .thenReturn(Optional.empty());
    }

    private void findByEmailContracts_mockExistingAndNotExistingAccount() {
        String existingAccountId = "178a715a-4d54-403d-b559-def430dd8a5b";
        String existingAccountId2 = "ead1f1a2-d178-4204-9ec1-b78c5bf6402c";
        Account account = new Account()
                .setId(existingAccountId)
                .setFamilyId(UUID.randomUUID().toString())
                .setFirstName("testFirstName")
                .setLastName("testLastName")
                .setEmail(existingEmailInFindByMailContracts)
                .setEnabled(true);
        Account account2 = new Account()
                .setId(existingAccountId2)
                .setFamilyId(UUID.randomUUID().toString())
                .setFirstName("testFirstName")
                .setLastName("testLastName")
                .setEmail(existingEmail)
                .setEnabled(true);
        when(accountRepository.findByEmail(existingEmailInFindByMailContracts.toLowerCase()))
                .thenReturn(Optional.of(account));
        when(accountRepository.findById(existingAccountId2))
                .thenReturn(Optional.of(account2));
        when(accountRepository.findByEmail(notExistingEmail))
                .thenReturn(Optional.empty());
    }

    private void saveAccountContracts_mockSuccessfulCreationOfAnAccount() {
        String accountIdAfterCreation = "bede3ade-a66d-47d4-9853-34e74287a725";
        Account account = new Account()
                .setId(accountIdAfterCreation)
                .setFirstName("testFirstName")
                .setLastName("testLastName")
                .setEmail(notExistingEmail)
                .setEnabled(false);
        when(accountRepository.findByEmail(notExistingEmail))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);
    }

    private void saveAccountContracts_mockConflictDuringCreationOfAnAccount() {
        when(accountRepository.findByEmail(existingEmail))
                .thenReturn(Optional.of(new Account()));
    }

    private void activateUserContracts_mockActivationCodeToPersist() {
        String accountId = "246b0ae2-d943-4d1a-a418-fdadfcb80455";
        ActivationCode activationCode = new ActivationCode()
                .setId(UUID.randomUUID().toString())
                .setAccountId(accountId)
                .setCreated(Instant.now())
                .setActivationCodeValue(UUID.randomUUID().toString());
        when(activationCodeService.save(any(ActivationCode.class)))
                .thenReturn(activationCode);
    }

    private void activateUserContracts__mockAccounts() {
        String accountId = "246b0ae2-d943-4d1a-a418-fdadfcb80455";
        String missingAccountId = "af138a5a-365c-4708-a0b9-0df76bd6b754";
        when(accountService.findById(accountId)).thenReturn(Optional.of(new Account()));
        when(accountService.findById(missingAccountId)).thenReturn(Optional.empty());
    }

    private void credentialsContracts_mockCredentialChecks() {
        String accountId = "b6e13d85-6c2e-4635-8f76-8b36a9184c86";
        Account acc = new Account()
                .setId(accountId)
                .setPassword("correctHashValue");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(acc));
    }

    private void activateUserContracts_mockNotExistingAccount() {
        String notExistingAccountId = "af138a5a-365c-4708-a0b9-0df76bd6b754";
        when(accountRepository.findById(notExistingAccountId)).thenReturn(Optional.empty());
    }

    private void familyAssignmentContracts_mockFamilyAssignments() {
        String existingFamilyId = "7dca5ea7-fa5c-4303-9569-c2f722a8fffa";
        String missingFamilyId = "83e61d08-7b6d-4520-bf73-7d9822bb5eca";
        String existingAccountId = "4987a33c-66c5-47e9-86a1-d4da60cc6561";
        String missingAccountId = "fa63ec5b-38c7-4d11-befb-0227df4cad1b";
        when(accountRepository.findById(existingAccountId)).thenReturn(Optional.of(new Account()));
        when(familyClient.isPresentById(existingFamilyId)).thenReturn(true);
        when(familyClient.isPresentById(missingFamilyId)).thenReturn(false);
    }
}
