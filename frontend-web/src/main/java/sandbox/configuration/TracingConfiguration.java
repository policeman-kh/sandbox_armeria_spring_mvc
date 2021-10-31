package sandbox.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linecorp.armeria.common.brave.RequestContextCurrentTraceContext;

import brave.Tracing;
import brave.http.HttpTracing;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Configuration
public class TracingConfiguration {
    @Bean
    public HttpTracing httpTracing() {
        final AsyncZipkinSpanHandler spanHandler =
                AsyncZipkinSpanHandler.create(OkHttpSender.create("http://localhost:9411/api/v2/spans"));
        final Tracing tracing = Tracing.newBuilder()
                                       .localServiceName("frontend-web")
                                       .currentTraceContext(RequestContextCurrentTraceContext.ofDefault())
                                       .addSpanHandler(spanHandler)
                                       .build();
        return HttpTracing.create(tracing);
    }
}
