package pl.konradboniecki.budget.accountmanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pl.konradboniecki.chassis.tools.ChassisSecurityBasicAuthHelper;
import pl.konradboniecki.chassis.tools.RestTools;

@Slf4j
@Data
@Service
public class FamilyClient {

    private RestTemplate restTemplate;
    @Value("${budget.baseUrl.familyManagement}")
    private String gatewayUrl;

    @Autowired
    public FamilyClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isPresentById(String familyId) {
        try {
            HttpHeaders headers = RestTools.defaultGetHTTPHeaders();
            headers.setBasicAuth(ChassisSecurityBasicAuthHelper.getEncodedCredentials());
            HttpEntity<?> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                    gatewayUrl + "/api/family-mgt/v1/families/" + familyId,
                    HttpMethod.GET,
                    httpEntity, JsonNode.class);
            return responseEntity.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            return false;
        }
    }
}
