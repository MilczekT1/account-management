package pl.konradboniecki.budget.accountmanagement.cucumber.commons;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class SharedData {
    public static final String MOCKED_MISSING_FAMILY_ID = "f759f9f0-645a-4841-a4d4-e0d05627182b";
    public static final String MOCKED_PRESENT_FAMILY_ID = "1021fe62-58ff-4cc4-8afb-79ce9703b9ed";

    private ResponseEntity<?> lastResponseEntity;
    private String lastCreatedPlainTextPassword;
    private String lastActivationCode;

    public HttpStatusCode getStatusCode() {
        return lastResponseEntity.getStatusCode();
    }

    @Getter
    private Map<String, String> emailToAccountIdMap = new HashMap<>();

    public String putEmailToAccountIdEntry(String email, String id) {
        email = email.toLowerCase();
        emailToAccountIdMap.putIfAbsent(email, id);
        return emailToAccountIdMap.get(email);
    }

    public String getAccountIdForEmail(String email) {
        return emailToAccountIdMap.get(email.toLowerCase());
    }

    public void clearAfterScenario() {
        emailToAccountIdMap.clear();
        lastCreatedPlainTextPassword = null;
        lastActivationCode = null;
    }
}
