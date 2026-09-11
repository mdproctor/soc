package io.casehub.soc.engine.compliance;

import io.casehub.api.engine.CaseHubRuntime;
import io.casehub.desiredstate.api.ActualState;
import io.casehub.desiredstate.api.DesiredStateGraph;
import io.casehub.desiredstate.api.GlobalReconciliationListener;
import io.casehub.desiredstate.api.NodeId;
import io.casehub.desiredstate.api.NodeStatus;
import io.casehub.soc.domain.SocContainmentNodeTypes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SocRecoveryVerificationListener implements GlobalReconciliationListener {

    private static final Logger LOG = Logger.getLogger(SocRecoveryVerificationListener.class);
    private static final String CASE_PREFIX = "case:";

    private final CaseHubRuntime caseHubRuntime;
    private final Map<String, VerificationState> states = new ConcurrentHashMap<>();

    @Inject
    public SocRecoveryVerificationListener(CaseHubRuntime caseHubRuntime) {
        this.caseHubRuntime = caseHubRuntime;
    }

    @Override
    public void onReconciliationCycleCompleted(String tenancyId, DesiredStateGraph desired, ActualState actual) {
        if (!tenancyId.startsWith(CASE_PREFIX)) {
            return;
        }

        String caseId = tenancyId.substring(CASE_PREFIX.length());
        VerificationState state = states.computeIfAbsent(tenancyId, k -> new VerificationState());

        Set<NodeId> containmentNodes = new HashSet<>();
        for (var entry : desired.nodes().entrySet()) {
            if (isContainmentType(entry.getValue().type())) {
                containmentNodes.add(entry.getKey());
            }
        }

        if (containmentNodes.isEmpty()) {
            return;
        }

        List<String> nowVerified = new ArrayList<>();
        List<String> stillDiverged = new ArrayList<>();

        for (NodeId nodeId : containmentNodes) {
            NodeStatus status = actual.statusOf(nodeId).orElse(NodeStatus.UNKNOWN);
            if (status == NodeStatus.PRESENT) {
                if (state.verifiedNodes.add(nodeId)) {
                    nowVerified.add(nodeId.value());
                    LOG.infof("Recovery verified: node %s converged for case %s", nodeId, caseId);
                }
            } else {
                stillDiverged.add(nodeId.value());
            }
        }

        if (state.verifiedNodes.containsAll(containmentNodes)) {
            var signal = new LinkedHashMap<String, Object>();
            signal.put("status", "verified");
            signal.put("convergenceTime", Instant.now().toString());
            signal.put("verifiedNodes", containmentNodes.stream().map(NodeId::value).toList());
            caseHubRuntime.signal(UUID.fromString(caseId), "recoveryVerification", signal);
            LOG.infof("All containment actions verified for case %s", caseId);
        }
    }

    @Override
    public void onTenantStopped(String tenancyId) {
        states.remove(tenancyId);
    }

    private boolean isContainmentType(io.casehub.desiredstate.api.NodeType type) {
        return type.equals(SocContainmentNodeTypes.ISOLATE_HOST)
                || type.equals(SocContainmentNodeTypes.BLOCK_IP)
                || type.equals(SocContainmentNodeTypes.BLOCK_DOMAIN)
                || type.equals(SocContainmentNodeTypes.REVOKE_CREDENTIALS)
                || type.equals(SocContainmentNodeTypes.ROTATE_API_KEY)
                || type.equals(SocContainmentNodeTypes.DISABLE_USER_ACCOUNT)
                || type.equals(SocContainmentNodeTypes.NETWORK_SEGMENTATION)
                || type.equals(SocContainmentNodeTypes.WIPE_ENDPOINT);
    }

    private static class VerificationState {
        final Set<NodeId> verifiedNodes = ConcurrentHashMap.newKeySet();
    }
}
