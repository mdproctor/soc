package io.casehub.soc.testing;

import io.casehub.neocortex.rag.runtime.DedupIngestionConfig;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@DefaultBean
@ApplicationScoped
public class TestDedupIngestionConfig implements DedupIngestionConfig {
    @Override public boolean enabled() { return false; }
    @Override public double threshold() { return 0.95; }
    @Override public Optional<String> logPath() { return Optional.empty(); }
}
