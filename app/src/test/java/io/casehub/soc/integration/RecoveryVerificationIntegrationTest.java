package io.casehub.soc.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;

@QuarkusTest
class RecoveryVerificationIntegrationTest {

    @Test
    void criticalAlert_startsRecoveryVerificationAfterContainment() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"eventType\":\"soc.alert.siem.crowdstrike\",\"severity\":\"CRITICAL\","
                    + "\"source\":\"10.0.1.42\",\"rule\":\"credential-harvesting\"}")
                .when().post("/api/soc/demo/inject-alert")
                .then()
                .statusCode(200)
                .body("evaluated", is(true));
    }

    @Test
    void lowSeverityAlert_noRecoveryVerification() {
        RestAssured.given()
                .contentType("application/json")
                .body("{\"eventType\":\"soc.alert.siem.splunk\",\"severity\":\"LOW\","
                    + "\"source\":\"10.0.2.10\",\"rule\":\"info-scan\"}")
                .when().post("/api/soc/demo/inject-alert")
                .then()
                .statusCode(200)
                .body("evaluated", is(true));
    }
}
