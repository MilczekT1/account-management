package pl.konradboniecki.budget.accountmanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import pl.konradboniecki.budget.accountmanagement.service.ActivationService;
import pl.konradboniecki.budget.openapi.api.AccountActivationApi;
import pl.konradboniecki.budget.openapi.dto.model.OASActivationCode;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Controller
public class AccountActivationController implements AccountActivationApi {

    private final ActivationService activationService;

    @Autowired
    public AccountActivationController(ActivationService activationService) {
        this.activationService = activationService;
    }

    @Override
    public ResponseEntity<Void> activateAccount(UUID accountId, UUID activationCode) throws Exception {
        String location = activationService.activateAccount(
                accountId.toString(), activationCode.toString());
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }

    @Override
    public ResponseEntity<OASActivationCode> createActivationCode(UUID accountId) throws Exception {
        OASActivationCode activationCode = activationService.createActivationCodeForAccountWithId(accountId.toString());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(activationCode);
    }
}
