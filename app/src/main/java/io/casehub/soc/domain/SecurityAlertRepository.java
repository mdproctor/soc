package io.casehub.soc.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SecurityAlertRepository {

    @Inject
    EntityManager em;

    @Transactional
    public void persist(SecurityAlertEntity entity) {
        em.persist(entity);
    }

    public Optional<SecurityAlertEntity> findById(UUID alertId) {
        return Optional.ofNullable(em.find(SecurityAlertEntity.class, alertId));
    }
}
