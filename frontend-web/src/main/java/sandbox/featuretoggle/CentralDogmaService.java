package sandbox.featuretoggle;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.centraldogma.client.CentralDogma;
import com.linecorp.centraldogma.common.Change;
import com.linecorp.centraldogma.common.Revision;

import io.reactivex.rxjava3.core.Completable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Component
public class CentralDogmaService {
    private static final String PROJECT_NAME = "project";
    private static final String REPOSITORY_NAME = "repository";
    private static final String PATH = "/featureToggle.json";

    private final CentralDogma centralDogma;

    @PostConstruct
    public void initialize() {
        createProject()
                .andThen(Completable.defer(this::createRepository))
                .andThen(Completable.defer(this::pushData))
                .blockingAwait();
    }

    private Completable createProject() {
        return Completable.fromFuture(centralDogma.createProject(PROJECT_NAME))
                          .doOnError(e -> log.error("Failed to create a project.", e))
                          .onErrorComplete();
    }

    private Completable createRepository() {
        return Completable.fromFuture(centralDogma.createRepository(PROJECT_NAME, REPOSITORY_NAME))
                          .doOnError(e -> log.error("Failed to create a repository.", e))
                          .onErrorComplete();
    }

    private Completable pushData() {
        String jsonStr = null;
        try {
            final FeatureFlag featureFlag = new FeatureFlag();
            featureFlag.setKey("testEnabled");
            featureFlag.setEnabled(false);
            jsonStr = new ObjectMapper().writeValueAsString(featureFlag);
        } catch (JsonProcessingException e) {
            log.error("Failed to make json string.", e);
            return Completable.complete();
        }
        return Completable.fromFuture(centralDogma.push(PROJECT_NAME, REPOSITORY_NAME, Revision.HEAD,
                                                        "push feature flag disabled",
                                                        Change.ofJsonUpsert(PATH, jsonStr)))
                          .doOnError(e -> log.error("Failed to push a data.", e))
                          .onErrorComplete();
    }

    @Data
    private static class FeatureFlag {
        private String key;
        private boolean enabled;
    }
}
