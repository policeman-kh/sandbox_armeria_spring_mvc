package sandbox.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.client.brave.BraveClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreaker;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerClient;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerListener;
import com.linecorp.armeria.client.circuitbreaker.CircuitBreakerRule;
import com.linecorp.armeria.client.endpoint.DynamicEndpointGroup;
import com.linecorp.armeria.client.endpoint.healthcheck.HealthCheckedEndpointGroup;
import com.linecorp.armeria.client.logging.LoggingClient;
import com.linecorp.armeria.client.retrofit2.ArmeriaRetrofit;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.logging.LogLevel;

import brave.http.HttpTracing;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import sandbox.BackendApiClient;

@Slf4j
@Configuration
public class ClientConfiguration {
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface ForSimpleClient {}

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface ForLoadBalancingClient {}

    @ForSimpleClient
    @Bean
    public BackendApiClient backendApiClient(MeterRegistry meterRegistry, HttpTracing tracing) {
        return ArmeriaRetrofit.builder("http://localhost:8081/")
                              .addConverterFactory(JacksonConverterFactory.create())
                              .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                              .decorator(circuitBreakerDecorator(meterRegistry))
                              .decorator(loggingClientDecorator())
                              .decorator(BraveClient.newDecorator(tracing.clientOf("backend-api")))
                              .build()
                              .create(BackendApiClient.class);
    }

    @ForLoadBalancingClient
    @Bean
    public BackendApiClient loadBalancingClient(DynamicEndpointGroup endpointGroup,
                                                MeterRegistry meterRegistry, HttpTracing tracing) {
        /*
        final EndpointGroup endpointGroup = EndpointGroup.of(
                Endpoint.of("localhost", 8081),
                Endpoint.of("localhost", 8082));
        */
        final HealthCheckedEndpointGroup healthCheckedGroup =
                HealthCheckedEndpointGroup.builder(endpointGroup, "/internal/l7check")
                                          .protocol(SessionProtocol.HTTP)
                                          .retryInterval(Duration.ofSeconds(10))
                                          .build();
        return ArmeriaRetrofit.builder(SessionProtocol.HTTP, healthCheckedGroup)
                              .addConverterFactory(JacksonConverterFactory.create())
                              .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                              .decorator(circuitBreakerDecorator(meterRegistry))
                              .decorator(loggingClientDecorator())
                              .decorator(BraveClient.newDecorator(tracing.clientOf("backend-web")))
                              .build()
                              .create(BackendApiClient.class);
    }

    private static Function<? super HttpClient, LoggingClient> loggingClientDecorator() {
        return LoggingClient.builder().logger(log).requestLogLevel(LogLevel.INFO).newDecorator();
    }

    private static Function<? super HttpClient, CircuitBreakerClient> circuitBreakerDecorator(
            MeterRegistry meterRegistry) {
        final CircuitBreakerRule rule = CircuitBreakerRule.builder()
                                                          .onServerErrorStatus()
                                                          .onException()
                                                          .thenFailure();
        final CircuitBreaker circuitBreaker =
                CircuitBreaker.builder("BackendApiClient")
                              .listener(CircuitBreakerListener.metricCollecting(meterRegistry))
                              .build();
        return CircuitBreakerClient.newDecorator(circuitBreaker, rule);
    }
}
