package io.casehub.soc;

import io.casehub.soc.domain.SecurityAlertEntity;
import io.casehub.soc.domain.SecurityAlertRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AlertIngestionResourceTest {

    @Inject
    SecurityAlertRepository repository;

    private static final String VALID_ALERT = """
            {
              "source": "crowdstrike",
              "sourceAlertId": "CS-2024-00123",
              "severity": "HIGH",
              "category": "malware",
              "affectedAssets": ["srv-web-01", "srv-db-02"],
              "description": "Cobalt Strike beacon detected on endpoint",
              "rawPayload": "{\\"detail\\": \\"raw crowdstrike event\\"}"
            }
            """;

    @Test
    void ingestAlert_validPayload_returns202WithAlertId() {
        String alertId = given()
                .contentType(ContentType.JSON)
                .body(VALID_ALERT)
        .when()
                .post("/api/alerts")
        .then()
                .statusCode(202)
                .body("alertId", notNullValue())
        .extract().path("alertId");

        assertNotNull(alertId);
        var entity = repository.findById(UUID.fromString(alertId));
        assertTrue(entity.isPresent(), "Alert must be persisted");
        assertEquals("crowdstrike", entity.get().getSource());
    }

    @Test
    void ingestAlert_minimalPayload_succeeds() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "source": "syslog",
                          "severity": "LOW"
                        }
                        """)
        .when()
                .post("/api/alerts")
        .then()
                .statusCode(202)
                .body("alertId", notNullValue());
    }

    @Test
    void ingestAlert_malformedJson_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("not-valid-json")
        .when()
                .post("/api/alerts")
        .then()
                .statusCode(400);
    }

    @Test
    void ingestAlert_missingSeverity_returns400or500() {
        int status = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "source": "syslog"
                        }
                        """)
        .when()
                .post("/api/alerts")
        .then()
        .extract().statusCode();

        assertTrue(status >= 400, "Missing severity should fail: " + status);
    }
}
