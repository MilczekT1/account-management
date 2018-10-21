package pl.konradboniecki.budget.accountmanagement.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.accountmanagement.exceptions.AccountNotFoundException;
import pl.konradboniecki.budget.accountmanagement.exceptions.FamilyNotFoundException;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.openapi.dto.model.OASAccount;
import pl.konradboniecki.budget.openapi.dto.model.OASAccountCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedAccount;
import pl.konradboniecki.chassis.tools.HashGenerator;

import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final FamilyClient familyClient;
    private final AccountMapper accountMapper;
    private final HashGenerator hashGenerator;

    public void changePassword(String newPassword, String accountId) {
        throwIfAccountNotFound(accountId);
        accountRepository.changePassword(newPassword, accountId);
    }

    public void activateAccountWith(String id) {
        accountRepository.setEnabled(id);
    }

    public OASCreatedAccount createAccount(OASAccountCreation newAccount) {
        String email = newAccount.getEmail().toLowerCase();
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        } else {
            Account accountToSave = accountMapper.toAccount(newAccount);
            Account acc = accountRepository.save(accountToSave);
            return accountMapper.toOASCreatedAccount(acc);
        }
    }

    public OASAccount findAccount(String idOrEmail, String findBy) {
        switch (findBy) {
            case "id":
                return accountMapper.toOASAccount(findByIdFromParam(idOrEmail));
            case "email":
                return accountMapper.toOASAccount(findByEmailFromParam(idOrEmail));
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid argument findBy=" + findBy + ", it should be \"id\" or \"email\"");
        }
    }

    public Account findByEmailFromParam(String email) {
        if (Account.isEmailValid(email.toLowerCase())) {
            Optional<Account> account = findByEmail(email);
            if (account.isPresent()) {
                return accountMapper.filterPassword(account.get());
            } else {
                throw new AccountNotFoundException("Account with email: " + email + " not found.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
        }
    }

    public Account findByIdFromParam(String id) {
        Optional<Account> account = findById(id);
        checkArgument(true, UUID.fromString(id));
        if (account.isPresent()) {
            return accountMapper.filterPassword(account.get());
        } else {
            throw new AccountNotFoundException("Account with id: " + id + " not found.");
        }
    }

    public void throwIfInvalidCredentialsCheck(String accountId, String password) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isPresent()) {
            String accountHashedPassword = accountOptional.get().getPassword();
            if (accountHashedPassword.equals(password)) {
                // do nothing
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new AccountNotFoundException("Account with id: " + accountId + " not found during credentials check.");
        }
    }

    public Optional<Account> findById(String id) {
        return accountRepository.findById(id);
    }

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email.toLowerCase());
    }

    public OASAccount assignAccountToFamily(String accountId, String familyId) {
        Optional<Account> account = findById(accountId);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Account with id: " + accountId + " not found");
        }
        if (familyClient.isPresentById(familyId)) {
            account.get().setFamilyId(familyId);
            Account savedAccount = accountRepository.save(account.get());
            return accountMapper.toOASAccount(savedAccount);
        } else {
            throw new FamilyNotFoundException("Family with id: " + familyId + " not found.");
        }
    }

    public void deleteAccountById(String accountId) {
        Long deleted = accountRepository.deleteAccountById(accountId);
        if (deleted == 0L) {
            throw new AccountNotFoundException("Account not found");
        }
    }

    private void throwIfAccountNotFound(String accountId) {
        if (findById(accountId).isEmpty()) {
            throw new AccountNotFoundException(
                    String.format("Account with id: %s not found.", accountId));
        }
    }
}
