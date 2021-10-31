package sandbox.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.armeria.server.ServiceNaming;
import com.linecorp.armeria.server.brave.BraveService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

import brave.Tracing;
import brave.http.HttpTracing;
import sandbox.BackendApiService;
import sandbox.JsonResponseConverter;

@Configuration
public class ArmeriaConfiguration {
    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public ArmeriaServerConfigurator ApiServiceBean(BackendApiService service, ObjectMapper objectMapper,
                                                    HttpTracing tracing) {
        return serverBuilder -> serverBuilder
                .annotatedService()
                .defaultServiceNaming(ServiceNaming.simpleTypeName())
                .responseConverters(new JsonResponseConverter(objectMapper))
                .decorator(BraveService.newDecorator(tracing))
                .build(service);
    }
}
