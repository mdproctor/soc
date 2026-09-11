package io.casehub.soc.domain;

import io.casehub.desiredstate.api.FaultEvent;
import io.casehub.desiredstate.api.FaultType;
import io.casehub.desiredstate.api.HumanGating;
import io.casehub.desiredstate.api.NodeId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocContainmentNodeSpecTest {

    @Test
    void eachSpecReturnsCorrectNodeType() {
        assertEquals(SocContainmentNodeTypes.ISOLATE_HOST,
                new SocContainmentNodeSpec.IsolateHostSpec("host1", "agent1").nodeType());
        assertEquals(SocContainmentNodeTypes.BLOCK_IP,
                new SocContainmentNodeSpec.BlockIpSpec("10.0.0.1", "rule1").nodeType());
        assertEquals(SocContainmentNodeTypes.BLOCK_DOMAIN,
                new SocContainmentNodeSpec.BlockDomainSpec("evil.com", "proxy1").nodeType());
        assertEquals(SocContainmentNodeTypes.REVOKE_CREDENTIALS,
                new SocContainmentNodeSpec.RevokeCredentialsSpec("cred1", "okta").nodeType());
        assertEquals(SocContainmentNodeTypes.ROTATE_API_KEY,
                new SocContainmentNodeSpec.RotateApiKeySpec("key1", "svc1").nodeType());
        assertEquals(SocContainmentNodeTypes.DISABLE_USER_ACCOUNT,
                new SocContainmentNodeSpec.DisableUserAccountSpec("user1", "ad").nodeType());
        assertEquals(SocContainmentNodeTypes.NETWORK_SEGMENTATION,
                new SocContainmentNodeSpec.NetworkSegmentationSpec("seg1", "fw1").nodeType());
        assertEquals(SocContainmentNodeTypes.WIPE_ENDPOINT,
                new SocContainmentNodeSpec.WipeEndpointSpec("host2", "agent2").nodeType());
    }

    @Test
    void divergenceReviewSpecReturnsReviewType() {
        var spec = SocDivergenceReviewSpec.fromFaultEvent(
                new FaultEvent(NodeId.of("test"), FaultType.PROVISION_FAILED, "test failure"),
                null);
        assertEquals(SocDivergenceReviewSpec.REVIEW_TYPE, spec.nodeType());
        assertEquals(HumanGating.ALL, spec.humanGating());
        assertEquals(NodeId.of("test"), spec.faultedNodeId());
        assertEquals("PROVISION_FAILED", spec.faultType());
    }
}
