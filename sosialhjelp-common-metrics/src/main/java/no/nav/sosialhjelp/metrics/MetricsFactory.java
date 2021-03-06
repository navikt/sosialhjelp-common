package no.nav.sosialhjelp.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import no.nav.sosialhjelp.metrics.proxy.EventProxy;
import no.nav.sosialhjelp.metrics.proxy.TimerProxy;

import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;
import static java.lang.reflect.Proxy.newProxyInstance;

public final class MetricsFactory {

    private MetricsFactory() {
    }

    private static final MetricsClient metricsClient = new MetricsClient();
    private static final PrometheusMeterRegistry prometheusMeterRegistry = new ProtectedPrometheusMeterRegistry();

    public static void enableMetrics(MetricsConfig metricsConfig) {
        MetricsClient.enableMetrics(metricsConfig);
    }

    public static Timer createTimer(String name) {
        return new Timer(metricsClient, name, new Timing() {});
    }

    public static Event createEvent(String name) {
        return new Event(metricsClient, name);
    }

    public static <T> T createTimerProxy(String name, T object, Class<T> type) {
        return createTimerProxyInstance(name, object, type);
    }

    public static <T> T createTimerProxyForWebService(String name, T object, Class<T> type) {
        return createTimerProxyInstance("ws." + name, object, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createTimerProxyInstance(String name, T object, Class<T> type) {
        ClassLoader classLoader = TimerProxy.class.getClassLoader();
        Class[] classes = {type};
        TimerProxy timerProxy = new TimerProxy(name, object);

        return (T) newProxyInstance(classLoader, classes, timerProxy);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createEventProxy(String name, T object, Class<T> type) {
        ClassLoader classLoader = EventProxy.class.getClassLoader();
        Class[] classes = {type};
        EventProxy eventProxy = new EventProxy(name, object);

        return (T) newProxyInstance(classLoader, classes, eventProxy);
    }

    public static MeterRegistry getMeterRegistry() {
        return prometheusMeterRegistry;
    }

    private static class ProtectedPrometheusMeterRegistry extends PrometheusMeterRegistry {
        public ProtectedPrometheusMeterRegistry() {
            super(DEFAULT);
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    }
}
