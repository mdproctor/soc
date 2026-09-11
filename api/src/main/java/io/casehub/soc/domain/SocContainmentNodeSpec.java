package io.casehub.soc.domain;

import io.casehub.desiredstate.api.NodeSpec;
import io.casehub.desiredstate.api.NodeType;

public sealed interface SocContainmentNodeSpec extends NodeSpec {

    record IsolateHostSpec(String hostname, String edpAgentId) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.ISOLATE_HOST; }
    }

    record BlockIpSpec(String ipAddress, String firewallRuleId) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.BLOCK_IP; }
    }

    record BlockDomainSpec(String domain, String proxyRuleId) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.BLOCK_DOMAIN; }
    }

    record RevokeCredentialsSpec(String credentialId, String iamProvider) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.REVOKE_CREDENTIALS; }
    }

    record RotateApiKeySpec(String keyId, String serviceId) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.ROTATE_API_KEY; }
    }

    record DisableUserAccountSpec(String userId, String iamProvider) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.DISABLE_USER_ACCOUNT; }
    }

    record NetworkSegmentationSpec(String segmentId, String firewallRuleId) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.NETWORK_SEGMENTATION; }
    }

    record WipeEndpointSpec(String hostname, String edpAgentId) implements SocContainmentNodeSpec {
        @Override public NodeType nodeType() { return SocContainmentNodeTypes.WIPE_ENDPOINT; }
    }
}
