package io.casehub.soc.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AttackTaxonomyTest {

    @Test
    void allFourteenTacticsPresent() {
        assertEquals(14, AttackTactic.values().length);
    }

    @Test
    void tacticMitreIdsAreUnique() {
        long uniqueIds = Arrays.stream(AttackTactic.values())
                .map(AttackTactic::mitreId)
                .distinct()
                .count();
        assertEquals(AttackTactic.values().length, uniqueIds);
    }

    @Test
    void tacticMitreIdsFollowTaPattern() {
        for (AttackTactic tactic : AttackTactic.values()) {
            assertTrue(tactic.mitreId().matches("TA\\d{4}"),
                    tactic.name() + " has invalid MITRE ID: " + tactic.mitreId());
        }
    }

    @Test
    void techniqueIdValidation() {
        assertDoesNotThrow(() ->
                new AttackTechnique("T1566", "Phishing", AttackTactic.INITIAL_ACCESS, null));
        assertDoesNotThrow(() ->
                new AttackTechnique("T1566.001", "Spearphishing Attachment",
                        AttackTactic.INITIAL_ACCESS, "T1566"));
    }

    @Test
    void invalidTechniqueIdRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new AttackTechnique("1566", "Bad", AttackTactic.INITIAL_ACCESS, null));
        assertThrows(IllegalArgumentException.class, () ->
                new AttackTechnique("T15", "Bad", AttackTactic.INITIAL_ACCESS, null));
        assertThrows(IllegalArgumentException.class, () ->
                new AttackTechnique("T1566.01", "Bad", AttackTactic.INITIAL_ACCESS, null));
    }

    @Test
    void subtechniqueRelationship() {
        var parent = new AttackTechnique("T1566", "Phishing",
                AttackTactic.INITIAL_ACCESS, null);
        var child = new AttackTechnique("T1566.001", "Spearphishing Attachment",
                AttackTactic.INITIAL_ACCESS, "T1566");

        assertFalse(parent.isSubtechnique());
        assertTrue(parent.parentTechnique().isEmpty());

        assertTrue(child.isSubtechnique());
        assertEquals("T1566", child.parentTechnique().orElseThrow());
    }

    @Test
    void nullRequiredFieldsRejected() {
        assertThrows(NullPointerException.class, () ->
                new AttackTechnique(null, "Phishing", AttackTactic.INITIAL_ACCESS, null));
        assertThrows(NullPointerException.class, () ->
                new AttackTechnique("T1566", null, AttackTactic.INITIAL_ACCESS, null));
        assertThrows(NullPointerException.class, () ->
                new AttackTechnique("T1566", "Phishing", null, null));
    }
}
