package pl.konradboniecki.budget.accountmanagement.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import pl.konradboniecki.budget.accountmanagement.cucumber.commons.SharedData;
import pl.konradboniecki.budget.accountmanagement.cucumber.security.Security;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;
import pl.konradboniecki.budget.accountmanagement.service.AccountMapper;
import pl.konradboniecki.budget.openapi.dto.model.OASAccount;
import pl.konradboniecki.budget.openapi.dto.model.OASAccountCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedAccount;
import pl.konradboniecki.budget.openapi.dto.model.OASPasswordModification;
import pl.konradboniecki.chassis.tools.HashGenerator;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@RequiredArgsConstructor
public class AccountSteps {

    final Security security;
    final TestRestTemplate testRestTemplate;
    final SharedData sharedData;
    final Environment environment;
    final AccountMapper accountMapper;

    @When("I create an account with properties:")
    public void iCreateAnAccountWithProperties(DataTable dataTable) {
        OASAccountCreation acp = (OASAccountCreation) dataTable.asList(OASAccountCreation.class).get(0);

        OASAccount account = createAccount(acp);
        if (sharedData.getStatusCode().is2xxSuccessful()) {
            sharedData.putEmailToAccountIdEntry(account.getEmail(), account.getId().toString());
        }
    }

    @And("account for email (.+) already exist$")
    public void accountForEmailAlreadyExist(String email) {
        if (findAccountByEmail(email).isEmpty()) {
            assertThat(sharedData.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            String password = "plainTextPassword";
            OASAccountCreation accountCreation = new OASAccountCreation()
                    .firstName("firstName")
                    .lastName("lastName")
                    .email(email)
                    .password(password);
            OASAccount acc = createAccount(accountCreation);
            assertThat(sharedData.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            sharedData.putEmailToAccountIdEntry(email, acc.getId().toString());
            sharedData.setLastCreatedPlainTextPassword(password);
        } else {
            assertThat(sharedData.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @And("response account does not contain password")
    public void responseAccountDoesNotContainPassword() {
        Account account = (Account) sharedData.getLastResponseEntity().getBody();
        assertThat(account).isNotNull();
        assertThat(account.getPassword()).isNull();
    }

    @And("created account does not contain password")
    public void createdAccountDoesNotContainPassword() {
        assertThat(sharedData.getLastResponseEntity().getBody()).isNotNull();
        String account = sharedData.getLastResponseEntity().getBody().toString();
        assertThat(StringUtils.containsIgnoreCase(account, "password"))
                .isFalse();
    }

    @When("I delete an account by id for email (.+)$")
    public void iDeleteAnAccountWithId(String email) {
        String accountId = sharedData.getAccountIdForEmail(email);
        deleteAccount(accountId);
    }

    private OASAccount createAccount(OASAccountCreation accountCreation) {
        HttpEntity<?> entity = new HttpEntity<>(accountCreation, security.getSecurityHeaders());
        ResponseEntity<OASCreatedAccount> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts", HttpMethod.POST, entity, OASCreatedAccount.class);
        sharedData.setLastResponseEntity(responseEntity);
        return toAccount(responseEntity.getBody());
    }

    private Optional<?> findAccountByEmail(String email) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{email}?findBy=email", HttpMethod.GET, entity, Account.class, email);
        sharedData.setLastResponseEntity(responseEntity);
        if (sharedData.getStatusCode().equals(HttpStatus.OK)) {
            return Optional.ofNullable(responseEntity.getBody());
        } else {
            return Optional.empty();
        }
    }

    private Optional<?> findAccountById(String accountId) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{accountId}", HttpMethod.GET, entity, Account.class, accountId);
        sharedData.setLastResponseEntity(responseEntity);
        if (sharedData.getStatusCode().equals(HttpStatus.OK)) {
            return Optional.ofNullable(responseEntity.getBody());
        } else {
            return Optional.empty();
        }
    }

    void deleteAccount(String accountId) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{accountId}", HttpMethod.DELETE, entity, Void.class, accountId);
        sharedData.setLastResponseEntity(responseEntity);
    }

    void addToFamily(String accountId, String familyId) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{accountId}/families/{familyId}", HttpMethod.PUT, entity, Void.class, accountId, familyId);
        sharedData.setLastResponseEntity(responseEntity);
    }

    @When("I delete random account")
    public void iDeleteRandomAccount() {
        deleteAccount(UUID.randomUUID().toString());
    }

    @When("I get an account by email (.+)$")
    public void iGetAnAccountByEmailTestMailCom(String email) {
        findAccountByEmail(email);
    }

    @When("I get an account by id for email (.+)$")
    public void iGetAnAccountByIdForEmailJohnDoeMailCom(String email) {
        String accountId = sharedData.getEmailToAccountIdMap().get(email.toLowerCase());
        findAccountById(accountId);
    }

    @When("I assign account with email (.+) to missing family$")
    public void iAssignAccountToMissingFamily(String email) {
        String accountId = sharedData.getEmailToAccountIdMap()
                .get(email.toLowerCase());
        String familyId = SharedData.MOCKED_MISSING_FAMILY_ID;
        addToFamily(accountId, familyId);
    }

    @When("I assign missing account to missing family")
    public void iAssignMissingAccountToMissingFamily() {
        String accountId = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();
        addToFamily(accountId, familyId);
    }

    @And("family 'BDDTestFamily' exists")
    public void familyTestFamilyExists() {
        boolean usingRealEnvironment = Arrays.stream(environment.getActiveProfiles())
                .anyMatch((x) -> x.equalsIgnoreCase("acceptance-tests"));
        if (usingRealEnvironment) {
            HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
            String familyId = SharedData.MOCKED_PRESENT_FAMILY_ID;
            ResponseEntity<?> responseEntity = testRestTemplate
                    .exchange("/api/family-mgt/v1/families/{familyId}", HttpMethod.GET, entity, Void.class, familyId);
            sharedData.setLastResponseEntity(responseEntity);
            HttpStatusCode status = sharedData.getStatusCode();
            assertThat(status.is2xxSuccessful()).isTrue();
        }
    }

    @When("I assign account with email (.+) to existing family 'BDDTestFamily'$")
    public void iAssignAccountWithEmailToExistingFamilyBDDTestFamily(String email) {
        String accountId = sharedData.getAccountIdForEmail(email);
        String familyId = SharedData.MOCKED_PRESENT_FAMILY_ID;
        addToFamily(accountId, familyId);
    }

    @When("I check (.+) password for account (.+)$")
    public void iCheckPasswordValidPasswordForAccountJohnDoeMailCom(String correctness, String email) {
        String accountId = sharedData.getEmailToAccountIdMap().get(email.toLowerCase());
        accountId = (accountId != null) ? accountId : UUID.randomUUID().toString();
        String password;
        if (correctness.equalsIgnoreCase("valid")) {
            password = new HashGenerator()
                    .hashPassword(sharedData.getLastCreatedPlainTextPassword());
        } else {
            password = UUID.randomUUID().toString();
        }

        HttpHeaders headers = security.getSecurityHeaders();
        headers.add("password", password);
        HttpEntity<?> entity = new HttpEntity<>(null, headers);
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{accountId}/credentials", HttpMethod.GET, entity, Void.class, accountId);
        sharedData.setLastResponseEntity(responseEntity);
        security.getSecurityHeaders().remove("password");
    }

    @When("I change password for account (.+)$")
    public void iChangePasswordForAccountJohnDoeMailCom(String email) {
        String accountId = Optional.ofNullable(sharedData.getAccountIdForEmail(email))
                .orElse(UUID.randomUUID().toString());
        OASPasswordModification passwordModification = new OASPasswordModification()
                .accountId(UUID.fromString(accountId))
                .newPassword(UUID.randomUUID().toString());
        HttpEntity<?> entity = new HttpEntity<>(passwordModification, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{accountId}/password", HttpMethod.PUT, entity, Void.class, accountId);
        sharedData.setLastResponseEntity(responseEntity);
    }

    @When("I create activation code for account (.+)$")
    public void iCreateActivationCodeForAccount(String email) {
        String accountId = sharedData.getAccountIdForEmail(email);
        createActivationCode(accountId);
    }

    @When("I create activation code for missing account (.+)$")
    public void iCreateActivationCodeForMissingAccount(String email) {
        String randomAccountId = UUID.randomUUID().toString();
        createActivationCode(randomAccountId);
    }

    private void createActivationCode(String accountId) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<ActivationCode> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{accountId}/activation-codes", HttpMethod.POST, entity, ActivationCode.class, accountId);
        sharedData.setLastResponseEntity(responseEntity);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            sharedData.setLastActivationCode(responseEntity.getBody().getActivationCodeValue());
        }
    }

    @When("I activate missing account (.+)$")
    public void iActivateAccountMissingJohnDoeMailCom(String email) {
        String randomAccountId = UUID.randomUUID().toString();
        String activationCode = UUID.randomUUID().toString();
        activateAccount(randomAccountId, activationCode);
    }

    private void activateAccount(String accountId, String activationCode) {
        HttpEntity<?> entity = new HttpEntity<>(null, security.getSecurityHeaders());
        ResponseEntity<?> responseEntity = testRestTemplate
                .exchange("/api/account-mgt/v1/accounts/{accountId}/activation-codes/{activationCode}", HttpMethod.GET, entity, Void.class, accountId, activationCode);
        sharedData.setLastResponseEntity(responseEntity);
    }

    @And("account (.+) is enabled$")
    public void accountMailIsEnabled(String email) {
        security.basicAuthentication();
        findAccountByEmail(email);
        security.unathorize();

        Account acc = (Account) sharedData.getLastResponseEntity().getBody();
        assertThat(acc).isNotNull();
        assertThat(acc.isEnabled()).isTrue();
    }

    @And("account (.+) is disabled$")
    public void accountMailIsDisabled(String email) {
        security.basicAuthentication();
        findAccountByEmail(email);
        security.unathorize();

        Account acc = (Account) sharedData.getLastResponseEntity().getBody();
        assertThat(acc).isNotNull();
        assertThat(acc.isEnabled()).isFalse();
    }

    @When("I activate account (.+)$")
    public void iActivateAccountMail(String email) {
        String accountId = sharedData.getAccountIdForEmail(email);
        String activationCode = sharedData.getLastActivationCode();
        activateAccount(accountId, activationCode);
    }

    @And("response contains activation code")
    public void responseContainsActivationCode() {
        ActivationCode activationCode = (ActivationCode) sharedData.getLastResponseEntity().getBody();

        assertThat(activationCode).isNotNull();
        assertThat(activationCode.getActivationCodeValue()).isNotEmpty();
    }

    @Given("account (.+) was already enabled$")
    public void accountMailWasAlreadyEnabled(String email) {
        String accountId = sharedData.getAccountIdForEmail(email);
        createActivationCode(accountId);
        String activationCode = sharedData.getLastActivationCode();
        activateAccount(accountId, activationCode);
    }

    @Given("new activation code and enabled account (.+)$")
    public void newActivationCodeAndEnabledAccount(String email) {
        String accountId = sharedData.getAccountIdForEmail(email);
        createActivationCode(accountId);
        String activationCode = sharedData.getLastActivationCode();
        activateAccount(accountId, activationCode);
    }

    public OASAccount toAccount(@NonNull OASCreatedAccount oasCreatedAccount) {
        UUID accountId = null;
        if (oasCreatedAccount.getId() != null) {
            accountId = oasCreatedAccount.getId();
        }
        boolean enabled = false;
        if (oasCreatedAccount.getEnabled() != null) {
            enabled = oasCreatedAccount.getEnabled();
        }
        return new OASAccount()
                .id(accountId)
                .firstName(oasCreatedAccount.getFirstName())
                .lastName(oasCreatedAccount.getLastName())
                .email(oasCreatedAccount.getEmail())
                .created(oasCreatedAccount.getCreated())
                .enabled(enabled)
                .familyId(null);
    }
}
