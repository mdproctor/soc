package io.casehub.soc.engine;

import io.casehub.desiredstate.api.ActualState;
import io.casehub.desiredstate.api.ActualStateAdapter;
import io.casehub.desiredstate.api.DesiredStateGraph;
import io.casehub.desiredstate.api.NodeId;
import io.casehub.desiredstate.api.NodeStatus;
import io.casehub.desiredstate.api.NodeType;
import io.casehub.soc.domain.SocDivergenceReviewSpec;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Set;

@ApplicationScoped
public class SocReviewNodeActualStateAdapter implements ActualStateAdapter {

    @Override
    public Set<NodeType> handledTypes() {
        return Set.of(SocDivergenceReviewSpec.REVIEW_TYPE);
    }

    @Override
    public ActualState readActual(DesiredStateGraph desired, String tenancyId) {
        var statuses = new HashMap<NodeId, NodeStatus>();
        for (var entry : desired.nodes().entrySet()) {
            if (SocDivergenceReviewSpec.REVIEW_TYPE.equals(entry.getValue().type())) {
                statuses.put(entry.getKey(), NodeStatus.ABSENT);
            }
        }
        return new ActualState(statuses);
    }
}
