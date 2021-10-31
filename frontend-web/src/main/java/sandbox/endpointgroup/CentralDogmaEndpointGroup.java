package sandbox.endpointgroup;

import java.util.List;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.DynamicEndpointGroup;
import com.linecorp.centraldogma.client.CentralDogma;

//@Component
public class CentralDogmaEndpointGroup extends DynamicEndpointGroup {
    private static final String PROJECT_NAME = "project";
    private static final String REPOSITORY_NAME = "repository";

    private final CentralDogma centralDogma;
    //private final Watcher<JsonNode> watcher;

    public CentralDogmaEndpointGroup(CentralDogma centralDogma) {
        setEndpoints(List.of(Endpoint.of("localhost", 8081),
                             Endpoint.of("localhost", 8082)));
        this.centralDogma = centralDogma;
        //watcher = centralDogma.fileWatcher(PROJECT_NAME, REPOSITORY_NAME, Query.ofJson(PATH));
    }

}
