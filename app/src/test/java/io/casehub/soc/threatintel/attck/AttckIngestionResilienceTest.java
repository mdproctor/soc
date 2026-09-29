package io.casehub.soc.threatintel.attck;

import io.casehub.neocortex.mindmap.MindMapCapability;
import io.casehub.neocortex.mindmap.MindMapStore;
import io.casehub.neocortex.mindmap.inmem.InMemoryMindMapStore;
import io.casehub.neocortex.rag.ChunkInput;
import io.casehub.neocortex.rag.CorpusRef;
import io.casehub.neocortex.rag.EmbeddingIngestor;
import io.casehub.neocortex.rag.testing.InMemoryEmbeddingIngestor;
import io.quarkus.runtime.StartupEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.casehub.soc.threatintel.attck.AttckConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

class AttckIngestionResilienceTest {

    @Test
    void crashRecovery_incompleteIngestion_reIngestsOnNextStartup() {
        var mindMapStore = new InMemoryMindMapStore();
        var ingestor = new InMemoryEmbeddingIngestor();
        var service = buildService(mindMapStore, ingestor);
        service.onStartup(new StartupEvent());

        var subgraphs = mindMapStore.listSubgraphs(REFERENCE_TENANT);
        var sg = subgraphs.stream().filter(s -> s.name().equals(SUBGRAPH_NAME)).findFirst().orElseThrow();
        mindMapStore.updateSubgraph(sg.id(), null, REFERENCE_TENANT);

        var service2 = buildService(mindMapStore, new InMemoryEmbeddingIngestor());
        service2.onStartup(new StartupEvent());

        var sg2 = mindMapStore.listSubgraphs(REFERENCE_TENANT).stream()
            .filter(s -> s.name().equals(SUBGRAPH_NAME)).findFirst().orElseThrow();
        assertThat(sg2.rootNodeId()).isNotNull();
    }

    @Test
    void ragFailureIsolation_mindMapStillAvailable() {
        var mindMapStore = new InMemoryMindMapStore();
        var failingIngestor = new InMemoryEmbeddingIngestor() {
            @Override
            public void ingest(CorpusRef corpus, List<ChunkInput> chunks) {
                throw new RuntimeException("RAG unavailable");
            }
        };
        var service = buildService(mindMapStore, failingIngestor);
        service.onStartup(new StartupEvent());

        assertThat(mindMapStore.resolveNode("T1003", null, REFERENCE_TENANT)).isNotNull();
    }

    @Test
    void deleteCorpusFailure_preventsRagDuplicates() {
        var mindMapStore = new InMemoryMindMapStore();
        var ingestor = new InMemoryEmbeddingIngestor();
        var service = buildService(mindMapStore, ingestor);
        service.onStartup(new StartupEvent());
        int initialChunks = ingestor.getChunks(new CorpusRef(REFERENCE_TENANT, CORPUS_NAME)).size();

        var failDeleteIngestor = new InMemoryEmbeddingIngestor() {
            @Override
            public void deleteCorpus(CorpusRef corpus) {
                throw new RuntimeException("deleteCorpus failed");
            }
        };
        var service2 = buildService(mindMapStore, failDeleteIngestor);
        mindMapStore.listSubgraphs(REFERENCE_TENANT).stream()
            .filter(s -> s.name().equals(SUBGRAPH_NAME))
            .findFirst()
            .ifPresent(sg -> {
                var rootNode = mindMapStore.getNode(sg.rootNodeId(), REFERENCE_TENANT);
                if (rootNode != null) {
                    mindMapStore.eraseSubgraph(sg.id(), REFERENCE_TENANT);
                }
            });
        service2.onStartup(new StartupEvent());

        int afterChunks = failDeleteIngestor.getChunks(new CorpusRef(REFERENCE_TENANT, CORPUS_NAME)).size();
        assertThat(afterChunks).isZero();
    }

    @Test
    void noSubgraphCapability_skipsIngestion() {
        var noSubgraphStore = new InMemoryMindMapStore() {
            @Override
            public Set<MindMapCapability> capabilities() {
                return Set.of();
            }
        };
        var ingestor = new InMemoryEmbeddingIngestor();
        var service = buildService(noSubgraphStore, ingestor);
        service.onStartup(new StartupEvent());

        assertThat(noSubgraphStore.listSubgraphs(REFERENCE_TENANT)).isEmpty();
        assertThat(ingestor.getChunks(new CorpusRef(REFERENCE_TENANT, CORPUS_NAME))).isEmpty();
    }

    @Test
    void disabledConfig_skipsIngestion() {
        var mindMapStore = new InMemoryMindMapStore();
        var ingestor = new InMemoryEmbeddingIngestor();
        var service = buildService(mindMapStore, ingestor);
        service.enabled = false;
        service.onStartup(new StartupEvent());

        assertThat(mindMapStore.listSubgraphs(REFERENCE_TENANT)).isEmpty();
    }

    @Test
    void reIngestion_versionChange() {
        var mindMapStore = new InMemoryMindMapStore();
        var ingestor1 = new InMemoryEmbeddingIngestor();
        var service1 = buildService(mindMapStore, ingestor1);
        service1.onStartup(new StartupEvent());

        var sg1 = mindMapStore.listSubgraphs(REFERENCE_TENANT).stream()
            .filter(s -> s.name().equals(SUBGRAPH_NAME)).findFirst().orElseThrow();
        var rootBefore = mindMapStore.getNode(sg1.rootNodeId(), REFERENCE_TENANT);
        String versionBefore = rootBefore.property("attck-version").orElse(null);
        assertThat(versionBefore).isNotNull();

        var ingestor2 = new InMemoryEmbeddingIngestor();
        var service2 = buildService(mindMapStore, ingestor2);
        service2.onStartup(new StartupEvent());

        var sg2 = mindMapStore.listSubgraphs(REFERENCE_TENANT).stream()
            .filter(s -> s.name().equals(SUBGRAPH_NAME)).findFirst().orElseThrow();
        var rootAfter = mindMapStore.getNode(sg2.rootNodeId(), REFERENCE_TENANT);
        assertThat(rootAfter.property("attck-version")).hasValue(versionBefore);
    }

    @Test
    void errorBoundary_appStartsEvenIfIngestionFails() {
        var failStore = new InMemoryMindMapStore() {
            @Override
            public String createSubgraph(io.casehub.neocortex.mindmap.SubgraphInput input, String tenantId) {
                throw new RuntimeException("MindMap unavailable");
            }
        };
        var service = buildService(failStore, new InMemoryEmbeddingIngestor());
        service.onStartup(new StartupEvent());
    }

    private static AttckIngestionService buildService(MindMapStore store, EmbeddingIngestor ingestor) {
        var service = new AttckIngestionService();
        service.enabled = true;
        service.mindMapStore = store;
        service.embeddingIngestor = ingestor;
        return service;
    }
}
