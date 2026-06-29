package io.casehub.soc.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * A MITRE ATT&CK technique identifier with tactic association.
 *
 * <p>Technique IDs follow MITRE's naming: T followed by 4 digits (e.g. T1566),
 * with optional sub-technique suffix (e.g. T1566.001).
 */
public record AttackTechnique(
        String techniqueId,
        String name,
        AttackTactic tactic,
        String subtechniqueOf) {

    public AttackTechnique {
        Objects.requireNonNull(techniqueId, "techniqueId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(tactic, "tactic");
        if (!techniqueId.matches("T\\d{4}(\\.\\d{3})?")) {
            throw new IllegalArgumentException(
                    "techniqueId must match T####[.###]: " + techniqueId);
        }
    }

    public boolean isSubtechnique() {
        return subtechniqueOf != null;
    }

    public Optional<String> parentTechnique() {
        return Optional.ofNullable(subtechniqueOf);
    }
}
