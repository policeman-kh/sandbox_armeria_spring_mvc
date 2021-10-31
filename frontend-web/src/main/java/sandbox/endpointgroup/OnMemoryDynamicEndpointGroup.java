package sandbox.endpointgroup;

import java.util.List;

import org.springframework.stereotype.Component;

import com.linecorp.armeria.client.Endpoint;
import com.linecorp.armeria.client.endpoint.DynamicEndpointGroup;
import com.linecorp.armeria.client.endpoint.EndpointSelectionStrategy;

@Component
public class OnMemoryDynamicEndpointGroup extends DynamicEndpointGroup {
    public OnMemoryDynamicEndpointGroup() {
        super(EndpointSelectionStrategy.rampingUp());
        setEndpoints(List.of(Endpoint.of("localhost", 8081),
                             Endpoint.of("localhost", 8082)));
    }

    public void addEndpoint() {
        addEndpoint(Endpoint.of("localhost", 8083));
    }
}
