package io.casehub.soc.engine;

import io.casehub.api.engine.YamlCaseHub;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SocCaseHub extends YamlCaseHub {

    public SocCaseHub() {
        super("soc/incident-investigation.yaml");
    }
}
