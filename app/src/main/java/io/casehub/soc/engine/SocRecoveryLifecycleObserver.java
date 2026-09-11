package io.casehub.soc.engine;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SocRecoveryLifecycleObserver {

    private static final Logger LOG = Logger.getLogger(SocRecoveryLifecycleObserver.class);
    private static final String CASE_PREFIX = "case:";

    // Observes CaseLifecycleEvent — stops reconciliation when case closes.
    // Full lifecycle integration deferred until LifecycleManager per-tenant
    // start/stop API is wired into the recovery verification worker.
    // For now, reconciliation loops are cleaned up by GlobalReconciliationListener.onTenantStopped().
}
