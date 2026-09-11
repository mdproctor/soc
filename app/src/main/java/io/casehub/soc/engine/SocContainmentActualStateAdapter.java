package io.casehub.soc.engine;

import io.casehub.desiredstate.api.ActualState;
import io.casehub.desiredstate.api.ActualStateAdapter;
import io.casehub.desiredstate.api.DesiredStateGraph;
import io.casehub.desiredstate.api.NodeId;
import io.casehub.desiredstate.api.NodeStatus;
import io.casehub.desiredstate.api.NodeType;
import io.casehub.soc.domain.SocContainmentNodeTypes;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Set;

@DefaultBean
@ApplicationScoped
public class SocContainmentActualStateAdapter implements ActualStateAdapter {

    private static final Set<NodeType> HANDLED = Set.of(
            SocContainmentNodeTypes.ISOLATE_HOST, SocContainmentNodeTypes.BLOCK_IP,
            SocContainmentNodeTypes.BLOCK_DOMAIN, SocContainmentNodeTypes.REVOKE_CREDENTIALS,
            SocContainmentNodeTypes.ROTATE_API_KEY, SocContainmentNodeTypes.DISABLE_USER_ACCOUNT,
            SocContainmentNodeTypes.NETWORK_SEGMENTATION, SocContainmentNodeTypes.WIPE_ENDPOINT);

    @Override
    public Set<NodeType> handledTypes() { return HANDLED; }

    @Override
    public ActualState readActual(DesiredStateGraph desired, String tenancyId) {
        var statuses = new HashMap<NodeId, NodeStatus>();
        for (var entry : desired.nodes().entrySet()) {
            if (HANDLED.contains(entry.getValue().type())) {
                statuses.put(entry.getKey(), NodeStatus.PRESENT);
            }
        }
        return new ActualState(statuses);
    }
}
