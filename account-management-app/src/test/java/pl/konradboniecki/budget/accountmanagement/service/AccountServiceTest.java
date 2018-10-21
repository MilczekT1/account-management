package pl.konradboniecki.budget.accountmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.accountmanagement.exceptions.FamilyNotFoundException;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.openapi.dto.model.OASAccount;
import pl.konradboniecki.budget.openapi.dto.model.OASAccountCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedAccount;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        properties = "spring.cloud.config.enabled=false"
)
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private FamilyClient familyClient;

    @Test
    public void givenInvalidId_whenFindById_thenExceptionIsThrown() {
        // Given:
        String invalidId = "blabla";
        // When:
        Throwable throwable = catchThrowableOfType(() -> accountService.findByIdFromParam(invalidId), Exception.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void whenActivateAccountWithId_thenRepoMethodIsInvoked() {
        // Given:
        String accountId = UUID.randomUUID().toString();
        doNothing().when(accountRepository).setEnabled(anyString());
        // When:
        accountService.activateAccountWith(accountId);
        // Then:
        verify(accountRepository, times(1)).setEnabled(accountId);
    }

    @Test
    public void givenAssignmentFamilyToAccount_whenFamilyFound_thenSetFamilyId() {
        // Given:
        String familyId = UUID.randomUUID().toString();
        String accountId = UUID.randomUUID().toString();
        Account accountToFind = new Account()
                .setId(accountId);
        Account accountToReturn = new Account()
                .setId(accountId)
                .setFamilyId(familyId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountToFind));
        when(familyClient.isPresentById(eq(familyId))).thenReturn(true);
        when(accountRepository.save(any(Account.class)))
                .thenReturn(accountToReturn);
        // When:
        OASAccount acc = accountService.assignAccountToFamily(accountId, familyId);
        // Then:
        assertThat(acc).isNotNull();
        assertThat(acc.getFamilyId().toString()).isEqualTo(familyId);
    }

    @Test
    public void givenAssignmentFamilyToAccount_whenFamilyNotFoundWithClientError_thenThrow() {
        // Given:
        String familyId = UUID.randomUUID().toString();
        String accountId = UUID.randomUUID().toString();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(new Account()));
        when(familyClient.isPresentById(familyId)).thenReturn(false);
        // When:
        Exception throwable = catchThrowableOfType(() ->
                        accountService.assignAccountToFamily(accountId, familyId),
                FamilyNotFoundException.class);
        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable).isInstanceOf(FamilyNotFoundException.class);
    }

    @Test
    void given_accountCreation_whenConflict_thenResponseWithConflict() {
        // Given:
        String presentEmail = "presentemail@mail.com";
        Account mockedAccount = new Account().setEmail(presentEmail);
        when(accountRepository.findByEmail(refEq(presentEmail)))
                .thenReturn(Optional.of(mockedAccount));
        OASAccountCreation accountCreation = new OASAccountCreation()
                .email(presentEmail);
        // When:
        ResponseStatusException throwable = catchThrowableOfType(() ->
                        accountService.createAccount(accountCreation),
                ResponseStatusException.class);

        // Then:
        assertThat(throwable).isNotNull();
        assertThat(throwable.getStatus()).isEqualTo(HttpStatus.CONFLICT);

    }

    @Test
    void given_accountCreation_whenSuccess_thenResponseWithAccount() {
        // Given:
        String notPresentEmail = "notPresentEmail@mail.com";
        String password = "thisShouldBeErased";
        Account mockedAccount = new Account()
                .setId(UUID.randomUUID().toString())
                .setFamilyId(UUID.randomUUID().toString())
                .setEmail(notPresentEmail)
                .setPassword(password);
        OASAccountCreation accountCreation = new OASAccountCreation()
                .email(notPresentEmail)
                .password("password");

        when(accountRepository.findByEmail(notPresentEmail))
                .thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class)))
                .thenReturn(mockedAccount);
        // When:
        OASCreatedAccount createdAccount = accountService.createAccount(accountCreation);
        // Then:
        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getEmail()).isEqualTo(notPresentEmail);
    }
}
