package pl.konradboniecki.budget.accountmanagement.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.konradboniecki.budget.accountmanagement.service.ActivationService;
import pl.konradboniecki.budget.openapi.api.AccountActivationApi;
import pl.konradboniecki.budget.openapi.dto.model.OASActivationCode;

import java.net.URI;
import java.util.UUID;

import static pl.konradboniecki.budget.accountmanagement.controller.AccountActivationController.BASE_PATH;

@Slf4j
@Controller
@RequestMapping(BASE_PATH)
public class AccountActivationController implements AccountActivationApi {
    public static final String BASE_PATH = "/api/account-mgt/v1";

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
