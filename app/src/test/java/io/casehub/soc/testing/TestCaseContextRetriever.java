package io.casehub.soc.testing;

import io.casehub.neocortex.rag.CaseContextRetriever;
import io.casehub.neocortex.rag.testing.InMemoryCaseRetriever;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.List;

@ApplicationScoped
public class TestCaseContextRetriever {

    @Produces
    @DefaultBean
    @ApplicationScoped
    CaseContextRetriever caseContextRetriever() {
        return new CaseContextRetriever(InMemoryCaseRetriever.returning(List.of()));
    }
}
