package pl.konradboniecki.budget.accountmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.REMOTE;
import static pl.konradboniecki.budget.accountmanagement.service.FamilyClientTest.*;

@ExtendWith(SpringExtension.class)
@TestInstance(PER_CLASS)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {
                "stubrunner.cloud.loadbalancer.enabled=false"
        }
)
@AutoConfigureStubRunner(
        repositoryRoot = "http://konradboniecki.com.pl:5001/repository/maven-public/",
        ids = {STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID + ":" + STUB_VERSION + ":stubs"},
        stubsMode = REMOTE
)
class FamilyClientTest {

    public static final String STUB_VERSION = "0.8.0-SNAPSHOT";
    public static final String STUB_GROUP_ID = "pl.konradboniecki.budget";
    public static final String STUB_ARTIFACT_ID = "family-management";
    @StubRunnerPort(STUB_GROUP_ID + ":" + STUB_ARTIFACT_ID)
    private Integer stubRunnerPort;

    @Autowired
    private FamilyClient familyClient;

    @BeforeEach
    void setUp() {
        familyClient.setGatewayUrl("http://localhost:" + stubRunnerPort);
    }

    @Test
    void returnTrueIfFamilyFound() {
        String presentFamilyId = "6e3c8e50-099a-4a44-9f63-0a6704937649";
        assertThat(familyClient.isPresentById(presentFamilyId)).isTrue();
    }

    @Test
    void given_notExistingId_whenFindById_thenReturnFalse() {
        String idOfNotExistingFamily = "ddad74b9-8fb3-4195-a999-07c01aaee371";
        Boolean result = familyClient.isPresentById(idOfNotExistingFamily);
        assertThat(result).isFalse();
    }
}
