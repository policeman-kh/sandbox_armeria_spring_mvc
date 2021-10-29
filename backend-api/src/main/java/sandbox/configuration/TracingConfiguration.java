package sandbox.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.common.brave.RequestContextCurrentTraceContext;

import brave.Tracing;
import brave.http.HttpTracing;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Configuration
public class TracingConfiguration {
    @Bean
    public Sender sender() {
        return OkHttpSender.create("http://localhost:9411/api/v2/spans");
    }

    @Bean
    public AsyncZipkinSpanHandler zipkinSpanHandler(Sender sender) {
        return AsyncZipkinSpanHandler.create(sender);
    }

    @Bean
    public Tracing tracing(AsyncZipkinSpanHandler zipkinSpanHandler) {
        return Tracing.newBuilder()
                      .localServiceName("backend-api")
                      .currentTraceContext(RequestContextCurrentTraceContext.ofDefault())
                      .addSpanHandler(zipkinSpanHandler)
                      .build();
    }

    @Bean
    public HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }
}
