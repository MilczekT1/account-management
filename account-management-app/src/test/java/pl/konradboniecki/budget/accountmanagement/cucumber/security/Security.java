package pl.konradboniecki.budget.accountmanagement.cucumber.security;

import org.springframework.http.HttpHeaders;

public interface Security {

    HttpHeaders getSecurityHeaders();

    void basicAuthentication();

    void unathorize();

}
