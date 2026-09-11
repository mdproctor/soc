package io.casehub.soc.engine;

import io.casehub.desiredstate.api.DeprovisionContext;
import io.casehub.desiredstate.api.DeprovisionResult;
import io.casehub.desiredstate.api.DesiredNode;
import io.casehub.desiredstate.api.NodeProvisioner;
import io.casehub.desiredstate.api.NodeType;
import io.casehub.desiredstate.api.ProvisionContext;
import io.casehub.desiredstate.api.ProvisionResult;
import io.casehub.soc.domain.SocContainmentNodeTypes;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class SocContainmentNodeProvisioner implements NodeProvisioner {

    private static final Set<NodeType> HANDLED = Set.of(
            SocContainmentNodeTypes.ISOLATE_HOST, SocContainmentNodeTypes.BLOCK_IP,
            SocContainmentNodeTypes.BLOCK_DOMAIN, SocContainmentNodeTypes.REVOKE_CREDENTIALS,
            SocContainmentNodeTypes.ROTATE_API_KEY, SocContainmentNodeTypes.DISABLE_USER_ACCOUNT,
            SocContainmentNodeTypes.NETWORK_SEGMENTATION, SocContainmentNodeTypes.WIPE_ENDPOINT);

    @Override
    public Set<NodeType> handledTypes() { return HANDLED; }

    @Override
    public Duration resyncInterval() { return Duration.ofMinutes(5); }

    @Override
    public ProvisionResult provision(DesiredNode node, ProvisionContext context) {
        return new ProvisionResult.Failed(
                "re-provisioning disabled — containment divergence requires human review");
    }

    @Override
    public DeprovisionResult deprovision(DesiredNode node, DeprovisionContext context) {
        return new DeprovisionResult.Failed(
                "containment node removal requires case closure");
    }
}
