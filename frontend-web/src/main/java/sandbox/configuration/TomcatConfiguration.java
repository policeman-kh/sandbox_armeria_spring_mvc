package sandbox.configuration;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.linecorp.armeria.server.brave.BraveService;
import com.linecorp.armeria.server.tomcat.TomcatService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;

import brave.Tracing;
import brave.http.HttpTracing;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

@Configuration
public class TomcatConfiguration {
    @Bean
    public TomcatService tomcatService(ServletWebServerApplicationContext applicationContext) {
        return TomcatService.of(getConnector(applicationContext));
    }

    @Bean
    public ArmeriaServerConfigurator armeriaServerConfigurator(TomcatService tomcatService,
                                                               MeterRegistry meterRegistry,
                                                               HttpTracing tracing) {
        return sb -> sb.service("prefix:/", tomcatService)
                       .decorator(BraveService.newDecorator(tracing))
                       .blockingTaskExecutor(newScheduledThreadPool(
                               32, meterRegistry, "armeria-blocking-executor"), false);
    }

    private static ScheduledExecutorService newScheduledThreadPool(int corePoolSize,
                                                                   MeterRegistry meterRegistry,
                                                                   String threadNamePrefix) {
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
                corePoolSize,
                new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d")
                                          .build());
        new ExecutorServiceMetrics(scheduledExecutorService, threadNamePrefix, List.of())
                .bindTo(meterRegistry);
        return scheduledExecutorService;
    }

    private static Connector getConnector(ServletWebServerApplicationContext applicationContext) {
        final TomcatWebServer container = (TomcatWebServer) applicationContext.getWebServer();

        // Start the container to make sure all connectors are available.
        container.start();
        return container.getTomcat().getConnector();
    }
}
