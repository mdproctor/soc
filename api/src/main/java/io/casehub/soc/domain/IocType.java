package io.casehub.soc.domain;

/**
 * Classification of observable artefact types used as Indicators of Compromise.
 */
public enum IocType {

    IP_ADDRESS,
    FILE_HASH_MD5,
    FILE_HASH_SHA1,
    FILE_HASH_SHA256,
    DOMAIN,
    URL,
    EMAIL,
    CVE,
    USER_AGENT,
    REGISTRY_KEY,
    MUTEX,
    CERTIFICATE_HASH
}
