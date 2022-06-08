package pl.konradboniecki.budget.accountmanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.konradboniecki.budget.accountmanagement.service.AccountService;
import pl.konradboniecki.budget.openapi.api.PasswordsApi;
import pl.konradboniecki.budget.openapi.dto.model.OASPasswordModification;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@RestController
public class PasswordController implements PasswordsApi {

    private final AccountService accountService;

    @Autowired
    public PasswordController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<Void> changePassword(UUID accountId, OASPasswordModification body) throws Exception {
        checkArgument(accountId.equals(body.getAccountId()));
        accountService.changePassword(
                body.getNewPassword(), body.getAccountId().toString());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> checkCredentials(UUID accountId, String password) throws Exception {
        accountService.throwIfInvalidCredentialsCheck(accountId.toString(), password);
        return ResponseEntity.ok().build();
    }
}
