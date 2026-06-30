package io.casehub.soc.engine;

import io.casehub.api.model.evaluator.JQExpressionEvaluator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SocCaseHubTest {

    @Inject
    SocCaseHub caseHub;

    @Test
    void definitionLoads() {
        var def = caseHub.getDefinition();
        assertNotNull(def);
        assertEquals("io.casehub.soc", def.getNamespace());
        assertEquals("incident-investigation", def.getName());
    }

    @Test
    void hasThreeCapabilities() {
        var names = caseHub.getDefinition().getCapabilities()
            .stream().map(c -> c.name()).toList();
        assertEquals(3, names.size());
        assertTrue(names.containsAll(List.of(
            "ioc-enrichment", "attck-mapping", "containment-recommendation")));
    }

    @Test
    void hasFourBindings() {
        var names = caseHub.getDefinition().getBindings()
            .stream().map(b -> b.getName()).toList();
        assertEquals(4, names.size());
        assertTrue(names.containsAll(List.of(
            "ioc-enrichment", "attck-mapping", "containment-recommendation", "analyst-review")));
    }

    @Test
    void hasThreeSuccessGoals() {
        var goals = caseHub.getDefinition().getGoals();
        assertEquals(3, goals.size());
        var goalNames = goals.stream().map(g -> g.getName()).toList();
        assertTrue(goalNames.containsAll(List.of("resolved", "escalated", "false-positive")));
    }

    @Test
    void resolvedGoalChecksAnalystDecision() {
        var resolved = caseHub.getDefinition().getGoals().stream()
            .filter(g -> "resolved".equals(g.getName()))
            .findFirst()
            .orElseThrow();
        assertTrue(resolved.getCondition() instanceof JQExpressionEvaluator jq
                && jq.expression().contains("analystDecision"),
            "Goal condition should check analystDecision");
    }
}
