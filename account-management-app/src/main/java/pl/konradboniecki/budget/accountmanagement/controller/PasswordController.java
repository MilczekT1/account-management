package pl.konradboniecki.budget.accountmanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.konradboniecki.budget.accountmanagement.service.AccountService;
import pl.konradboniecki.budget.openapi.api.PasswordsApi;
import pl.konradboniecki.budget.openapi.dto.model.OASPasswordModification;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static pl.konradboniecki.budget.accountmanagement.controller.PasswordController.BASE_PATH;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
public class PasswordController implements PasswordsApi {
    public static final String BASE_PATH = "/api/account-mgt/v1";

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
