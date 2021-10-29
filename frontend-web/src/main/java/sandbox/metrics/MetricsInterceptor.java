package sandbox.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.linecorp.armeria.common.metric.MeterIdPrefix;
import com.linecorp.armeria.common.metric.MoreMeters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Component
public class MetricsInterceptor implements HandlerInterceptor {
    private final MeterRegistry meterRegistry;
    private final Map<HandlerMethodInfo, MethodMetrics> metricsCache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        final Sample sample = Timer.start();
        final HandlerMethod handlerMethod = (HandlerMethod) handler;

        final String serviceName = handlerMethod.getBeanType().getSimpleName();
        final String methodName = handlerMethod.getMethod().getName();
        final HandlerMethodInfo handlerMethodInfo = new HandlerMethodInfo(serviceName, methodName);
        final MethodMetrics methodMetrics = metricsCache.computeIfAbsent(
                handlerMethodInfo, this::getMethodMetrics);
        request.setAttribute("serviceMetrics", new Metrics(sample, methodMetrics));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        final Metrics metrics = (Metrics) request.getAttribute("serviceMetrics");
        if (metrics == null) {
            return;
        }
        final MethodMetrics methodMetrics = metrics.getMethodMetrics();
        if (ex != null || response.getStatus() > 200) {
            methodMetrics.error(metrics.getSample(), null);
        } else {
            methodMetrics.success(metrics.getSample());
        }
    }

    private MethodMetrics getMethodMetrics(HandlerMethodInfo handlerMethodInfo) {
        final MeterIdPrefix meterIdPrefix = new MeterIdPrefix("armeria.server")
                .withTags("service", handlerMethodInfo.getServiceName())
                .withTags("method", handlerMethodInfo.getMethodName())
                .withTags("http.status", "");
        final String name = meterIdPrefix.name("requests");
        final Counter successes =
                meterRegistry.counter(name, meterIdPrefix.tags("result", "success"));
        final Counter failures =
                meterRegistry.counter(name, meterIdPrefix.tags("result", "failure"));
        final Timer requests = MoreMeters.newTimer(
                meterRegistry, meterIdPrefix.name("total.duration"), meterIdPrefix.tags());
        return new MethodMetrics(requests, successes, failures);
    }

    @Value
    private static class MethodMetrics {
        Timer timer;
        Counter successes;
        Counter failures;

        public void success(Sample sample) {
            successes.increment();
            sample.stop(timer);
        }

        public void error(Sample sample, Throwable t) {
            failures.increment();
            sample.stop(timer);
        }
    }

    @Value
    private static class HandlerMethodInfo {
        String serviceName;
        String methodName;
    }

    @Value
    private static class Metrics {
        Sample sample;
        MethodMetrics methodMetrics;
    }
}
