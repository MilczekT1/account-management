package pl.konradboniecki.budget.accountmanagement.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
class AccountTest {

    @Test
    void testIfHasFamily() {
        Account acc = new Account();
        assertFalse(acc.hasFamily());
        acc.setFamilyId(UUID.randomUUID().toString());
        assertTrue(acc.hasFamily());
    }

    @Test
    void testEmailValidation() {
        // TODO: edge cases
        assertTrue(Account.isEmailValid("konrad_boniecki@hotmail.com"));
        assertFalse(Account.isEmailValid(""));
        assertFalse(Account.isEmailValid(null));
    }
}
