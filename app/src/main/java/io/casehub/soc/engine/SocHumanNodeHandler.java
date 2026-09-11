package io.casehub.soc.engine;

import io.casehub.desiredstate.api.DesiredNode;
import io.casehub.desiredstate.api.HumanNodeHandler;
import io.casehub.desiredstate.api.ProvisionContext;
import io.casehub.desiredstate.api.StepOutcome;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@Alternative
@ApplicationScoped
public class SocHumanNodeHandler implements HumanNodeHandler {

    private static final Logger LOG = Logger.getLogger(SocHumanNodeHandler.class);

    @Override
    public StepOutcome onProvision(DesiredNode node, ProvisionContext context) {
        LOG.infof("Human-gated review node provisioned: %s (type: %s, tenancy: %s)",
                node.id(), node.type(), context.tenancyId());
        return new StepOutcome.Succeeded();
    }
}
