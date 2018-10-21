package pl.konradboniecki.budget.accountmanagement.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.accountmanagement.model.Account;
import pl.konradboniecki.budget.openapi.dto.model.OASAccount;
import pl.konradboniecki.budget.openapi.dto.model.OASAccountCreation;
import pl.konradboniecki.budget.openapi.dto.model.OASCreatedAccount;
import pl.konradboniecki.chassis.tools.HashGenerator;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountMapper {

    private HashGenerator hashGenerator;

    public Account toAccount(OASAccountCreation acp) {
        return new Account()
                .setFirstName(acp.getFirstName())
                .setLastName(acp.getLastName())
                .setEmail(acp.getEmail().toLowerCase())
                .setPassword(hashGenerator.hashPassword(acp.getPassword()))
                .setFamilyId(null)
                .setCreated(Instant.now())
                .setEnabled(false);
    }

    public OASCreatedAccount toOASCreatedAccount(Account account) {
        return new OASCreatedAccount()
                .id(UUID.fromString(account.getId()))
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .created(account.getCreated())
                .enabled(account.isEnabled());
    }

    public OASAccount toOASAccount(Account account) {
        UUID familyId = null;
        if (account.getFamilyId() != null) {
            familyId = UUID.fromString(account.getFamilyId());
        }
        return new OASAccount()
                .id(UUID.fromString(account.getId()))
                .familyId(familyId)
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .email(account.getEmail())
                .created(account.getCreated())
                .enabled(account.isEnabled());
    }

    public Account filterPassword(Account account) {
        return account.setPassword(null);
    }
}
