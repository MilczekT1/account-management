package pl.konradboniecki.budget.accountmanagement.cucumber.commons;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class CucumberInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest outboundRequest, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("Sending -> {}, {}", outboundRequest.getMethod(), outboundRequest.getURI());
        ClientHttpResponse response = execution.execute(outboundRequest, body);
        log.info("Received -> {}", response.getStatusCode());
        return response;
    }
}
