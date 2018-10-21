package pl.konradboniecki.budget.accountmanagement.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ActivationCodeTest {

    @Test
    void testUserActivationCodeInit() {
        String accountId = UUID.randomUUID().toString();
        String activationCode = "234regdf";
        ActivationCode userActivationCode = new ActivationCode(accountId, activationCode);

        assertAll(
                () -> assertNull(userActivationCode.getId()),
                () -> assertEquals(accountId, userActivationCode.getAccountId()),
                () -> assertEquals(activationCode, userActivationCode.getActivationCode()),
                () -> assertNotNull(userActivationCode.getCreated())
        );
    }
}
