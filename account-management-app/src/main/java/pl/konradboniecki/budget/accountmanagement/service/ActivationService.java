package pl.konradboniecki.budget.accountmanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.konradboniecki.budget.accountmanagement.exceptions.AccountNotFoundException;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;
import pl.konradboniecki.budget.openapi.dto.model.OASActivationCode;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ActivationService {

    private final AccountService accountService;
    private final ActivationCodeService activationCodeService;
    @Value("${budget.baseUrl.gateway}")
    private String BASE_URL;

    @Autowired
    public ActivationService(AccountService accountService, ActivationCodeService activationCodeService) {
        this.accountService = accountService;
        this.activationCodeService = activationCodeService;
    }

    public String activateAccount(String id, String activationCodeFromUrl) {
        Optional<Account> acc = accountService.findById(id);
        if (acc.isEmpty()) {
            return BASE_URL + "/register";
        }
        if (acc.get().isEnabled()) {
            return BASE_URL + "/login";
        }

        Optional<ActivationCode> activationCode =
                activationCodeService.findByAccountId(acc.get().getId());

        if (activationCodeIsPresentAndMatchesWithUrl(activationCode, activationCodeFromUrl)) {
            accountService.activateAccountWith(id);
            log.info("User with ID: " + acc.get().getId() + " has been activated");
            activationCodeService.deleteById(activationCode.get().getId());
            return BASE_URL + "/login";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid activation link");
    }

    public OASActivationCode createActivationCodeForAccountWithId(String accountId) {
        if (accountService.findById(accountId).isEmpty()) {
            throw new AccountNotFoundException("Account not found. id: " + accountId);
        }
        //TODO: override existing activationCode
        // and remove cleanup service
        ActivationCode activationCodeToSave = new ActivationCode()
                .setAccountId(accountId)
                .setCreated(Instant.now())
                .setActivationCode(UUID.randomUUID().toString());
        return toOASActivationCode(activationCodeService.save(activationCodeToSave));
    }

    private boolean activationCodeIsPresentAndMatchesWithUrl(Optional<ActivationCode> activationCode, String activationCodeFromUrl) {
        return activationCode.isPresent()
                && activationCode.get().getActivationCode().equals(activationCodeFromUrl);
    }

    private OASActivationCode toOASActivationCode(ActivationCode activationCode) {
        return new OASActivationCode()
                .id(UUID.fromString(activationCode.getId()))
                .accountId(UUID.fromString(activationCode.getAccountId()))
                .activationCode(UUID.fromString(activationCode.getActivationCode()))
                .created(activationCode.getCreated());
    }
}
