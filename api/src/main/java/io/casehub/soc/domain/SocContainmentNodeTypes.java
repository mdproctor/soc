package io.casehub.soc.domain;

import io.casehub.desiredstate.api.NodeType;

public final class SocContainmentNodeTypes {
    public static final NodeType ISOLATE_HOST = NodeType.of("soc:isolate-host");
    public static final NodeType BLOCK_IP = NodeType.of("soc:block-ip");
    public static final NodeType BLOCK_DOMAIN = NodeType.of("soc:block-domain");
    public static final NodeType REVOKE_CREDENTIALS = NodeType.of("soc:revoke-credentials");
    public static final NodeType ROTATE_API_KEY = NodeType.of("soc:rotate-api-key");
    public static final NodeType DISABLE_USER_ACCOUNT = NodeType.of("soc:disable-user-account");
    public static final NodeType NETWORK_SEGMENTATION = NodeType.of("soc:network-segmentation");
    public static final NodeType WIPE_ENDPOINT = NodeType.of("soc:wipe-endpoint");

    private SocContainmentNodeTypes() {}
}
