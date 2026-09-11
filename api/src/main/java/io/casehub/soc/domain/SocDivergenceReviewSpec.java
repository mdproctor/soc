package io.casehub.soc.domain;

import io.casehub.desiredstate.api.DesiredStateGraph;
import io.casehub.desiredstate.api.FaultEvent;
import io.casehub.desiredstate.api.HumanGating;
import io.casehub.desiredstate.api.NodeId;
import io.casehub.desiredstate.api.NodeSpec;
import io.casehub.desiredstate.api.NodeType;

public record SocDivergenceReviewSpec(
    NodeId faultedNodeId,
    String faultType,
    String faultMessage
) implements NodeSpec {

    public static final NodeType REVIEW_TYPE = NodeType.of("soc:divergence-review");

    @Override
    public NodeType nodeType() { return REVIEW_TYPE; }

    @Override
    public HumanGating humanGating() { return HumanGating.ALL; }

    public static SocDivergenceReviewSpec fromFaultEvent(FaultEvent event, DesiredStateGraph current) {
        return new SocDivergenceReviewSpec(event.node(), event.type().name(), event.detail());
    }
}
