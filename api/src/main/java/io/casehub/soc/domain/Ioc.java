package io.casehub.soc.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * An Indicator of Compromise — an observable artefact associated with malicious activity.
 *
 * <p>Equality is by {@code (type, value)} — two IOCs with the same type and value
 * are the same indicator regardless of when or where they were observed.
 */
public record Ioc(
        IocType type,
        String value,
        double confidence,
        Instant firstSeen,
        String source,
        Set<String> tags) {

    public Ioc {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) throw new IllegalArgumentException("value must not be blank");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be in [0.0, 1.0]: " + confidence);
        }
        tags = tags == null ? Set.of() : Set.copyOf(tags);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Ioc other
                && type == other.type
                && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
