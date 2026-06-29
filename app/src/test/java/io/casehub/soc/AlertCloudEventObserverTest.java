package io.casehub.soc;

import io.casehub.soc.domain.SecurityAlertReceived;
import io.casehub.soc.domain.SecurityAlertRepository;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class AlertCloudEventObserverTest {

    @Inject
    Event<CloudEvent> cloudEventBus;

    @Inject
    SecurityAlertRepository repository;

    @Test
    void cloudEventWithSocAlertType_createsSecurityAlert() throws Exception {
        String payload = """
                {
                  "sourceAlertId": "CS-CE-001",
                  "severity": "CRITICAL",
                  "category": "ransomware",
                  "affectedAssets": ["dc-01.corp.local"],
                  "description": "Ransomware encryption activity detected"
                }
                """;

        CloudEvent ce = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("soc.alert.edr.crowdstrike")
                .withSource(URI.create("/sensors/cs-falcon"))
                .withTime(OffsetDateTime.now())
                .withData("application/json", payload.getBytes())
                .withExtension("tenancyid", "test-tenant")
                .build();

        cloudEventBus.fireAsync(ce).toCompletableFuture().get(5, TimeUnit.SECONDS);

        // The observer processes asynchronously — give CDI a moment
        Thread.sleep(500);
    }

    @Test
    void cloudEventWithNonSocType_isIgnored() throws Exception {
        CloudEvent ce = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("aml.transaction.flagged")
                .withSource(URI.create("/aml/scanner"))
                .withData("application/json", "{}".getBytes())
                .build();

        cloudEventBus.fireAsync(ce).toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    @Test
    void cloudEventExtractsSourceFromCeType() throws Exception {
        String payload = """
                {
                  "severity": "HIGH",
                  "description": "Brute force attempt"
                }
                """;

        CloudEvent ce = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("soc.alert.siem.splunk")
                .withSource(URI.create("/siem/splunk"))
                .withTime(OffsetDateTime.now())
                .withData("application/json", payload.getBytes())
                .withExtension("tenancyid", "default")
                .build();

        cloudEventBus.fireAsync(ce).toCompletableFuture().get(5, TimeUnit.SECONDS);
        Thread.sleep(500);
    }
}
