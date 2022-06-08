package pl.konradboniecki.budget.accountmanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.konradboniecki.budget.accountmanagement.service.AccountService;
import pl.konradboniecki.budget.openapi.api.AccountManagementApi;
import pl.konradboniecki.budget.openapi.dto.model.OASAccount;
import pl.konradboniecki.budget.openapi.dto.model.OASAccountCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedAccount;

import java.util.UUID;

@Slf4j
@RestController
public class AccountManagementController implements AccountManagementApi {

    private final AccountService accountService;

    @Autowired
    public AccountManagementController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<OASAccount> findAccount(String accountIdOrEmail, String findBy) throws Exception {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountService.findAccount(accountIdOrEmail, findBy));
    }

    @Override
    public ResponseEntity<OASCreatedAccount> createAccount(OASAccountCreation oaSAccountCreation) throws Exception {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountService.createAccount(oaSAccountCreation));
    }

    @Override
    public ResponseEntity<Void> deleteAccount(UUID accountId) throws Exception {
        accountService.deleteAccountById(accountId.toString());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OASAccount> assignAccountToFamily(UUID accountId, UUID familyId) throws Exception {
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountService.assignAccountToFamily(accountId.toString(), familyId.toString()));
    }
}
