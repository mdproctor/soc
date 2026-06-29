package io.casehub.soc.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IocTest {

    @Test
    void equalityByTypeAndValue() {
        var ioc1 = new Ioc(IocType.IP_ADDRESS, "192.168.1.100", 0.9,
                Instant.now(), "crowdstrike", Set.of("c2"));
        var ioc2 = new Ioc(IocType.IP_ADDRESS, "192.168.1.100", 0.5,
                Instant.now().minusSeconds(3600), "splunk", Set.of("malware"));

        assertEquals(ioc1, ioc2);
        assertEquals(ioc1.hashCode(), ioc2.hashCode());
    }

    @Test
    void differentTypeOrValueAreNotEqual() {
        var ipIoc = new Ioc(IocType.IP_ADDRESS, "192.168.1.100", 0.9,
                null, "test", null);
        var domainIoc = new Ioc(IocType.DOMAIN, "192.168.1.100", 0.9,
                null, "test", null);
        var differentIp = new Ioc(IocType.IP_ADDRESS, "10.0.0.1", 0.9,
                null, "test", null);

        assertNotEquals(ipIoc, domainIoc);
        assertNotEquals(ipIoc, differentIp);
    }

    @Test
    void worksCorrectlyInSets() {
        var ioc1 = new Ioc(IocType.FILE_HASH_SHA256, "abc123", 0.95, null, "vt", null);
        var ioc2 = new Ioc(IocType.FILE_HASH_SHA256, "abc123", 0.80, null, "misp", null);
        var ioc3 = new Ioc(IocType.FILE_HASH_SHA256, "def456", 0.70, null, "vt", null);

        var set = new HashSet<Ioc>();
        set.add(ioc1);
        set.add(ioc2);
        set.add(ioc3);
        assertEquals(2, set.size());
    }

    @Test
    void blankValueRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new Ioc(IocType.IP_ADDRESS, "  ", 0.5, null, "test", null));
    }

    @Test
    void confidenceOutOfRangeRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new Ioc(IocType.IP_ADDRESS, "1.2.3.4", 1.5, null, "test", null));
        assertThrows(IllegalArgumentException.class, () ->
                new Ioc(IocType.IP_ADDRESS, "1.2.3.4", -0.1, null, "test", null));
    }

    @Test
    void boundaryConfidenceAccepted() {
        assertDoesNotThrow(() ->
                new Ioc(IocType.IP_ADDRESS, "1.2.3.4", 0.0, null, "test", null));
        assertDoesNotThrow(() ->
                new Ioc(IocType.IP_ADDRESS, "1.2.3.4", 1.0, null, "test", null));
    }

    @Test
    void tagsDefensivelyCopied() {
        var mutable = new java.util.HashSet<>(Set.of("c2", "malware"));
        var ioc = new Ioc(IocType.DOMAIN, "evil.com", 0.9, null, "test", mutable);

        mutable.add("phishing");
        assertEquals(2, ioc.tags().size());
        assertThrows(UnsupportedOperationException.class,
                () -> ioc.tags().add("phishing"));
    }

    @Test
    void nullTagsDefaultToEmpty() {
        var ioc = new Ioc(IocType.URL, "http://bad.com", 0.5, null, "test", null);
        assertEquals(Set.of(), ioc.tags());
    }
}
