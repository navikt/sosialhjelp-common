package no.nav.sosialhjelp.metrics;

import java.util.Optional;

public class MetricsConfig {

    public static final String SENSU_CLIENT_HOST = "sensu_client_host";
    public static final String SENSU_CLIENT_PORT = "sensu_client_port";

    public static final String SENSU_RETRY_INTERVAL_PROPERTY_NAME = "metrics.sensu.report.retryInterval";
    public static final String SENSU_QUEUE_SIZE_PROPERTY_NAME = "metrics.sensu.report.queueSize";
    public static final String SENSU_BATCHES_PER_SECOND_PROPERTY_NAME = "metrics.sensu.report.batchesPerSecond";
    public static final String SENSU_BATCH_SIZE_PROPERTY_NAME = "metrics.sensu.report.batchSize";
    public static final String SENSU_CONNECT_TIMEOUT_PROPERTY_NAME = "metrics.sensu.report.connectTimeout";

    private String sensuHost;
    private int sensuPort;

    private String application;
    private String hostname;
    private String environment;

    private int retryInterval;
    private int queueSize;
    private int batchesPerSecond;
    private int batchSize;
    private int connectTimeout;

    public static MetricsConfig resolveNaisConfig(String applicationName, String environmentName, String hostname) {
        return defaultConfig("sensu.nais", 3030, applicationName, environmentName, hostname);
    }

    private static MetricsConfig defaultConfig(String host, int port, String applicationName, String environmentName, String hostname) {
        return withSensuDefaults(new MetricsConfigBuilder()
                .sensuHost(getOptionalProperty(SENSU_CLIENT_HOST).orElse(host))
                .sensuPort(getOptionalProperty(SENSU_CLIENT_PORT).map(Integer::parseInt).orElse(port))
                .application(applicationName)
                .environment(environmentName)
                .hostname(hostname)
                .build()
        );
    }

    public static MetricsConfig withSensuDefaults(MetricsConfig metricsConfig) {
        return metricsConfig
                .withRetryInterval(defaultIntSystemProperty(SENSU_RETRY_INTERVAL_PROPERTY_NAME, 1000))
                .withQueueSize(defaultIntSystemProperty(SENSU_QUEUE_SIZE_PROPERTY_NAME, 20_000))
                .withBatchesPerSecond(defaultIntSystemProperty(SENSU_BATCHES_PER_SECOND_PROPERTY_NAME, 20))
                .withBatchSize(defaultIntSystemProperty(SENSU_BATCH_SIZE_PROPERTY_NAME, 100))
                .withConnectTimeout(defaultIntSystemProperty(SENSU_CONNECT_TIMEOUT_PROPERTY_NAME, 1000));
    }

    private static int defaultIntSystemProperty(String propertyName, int defaultValue) {
        return Integer.parseInt(System.getProperty(propertyName, Integer.toString(defaultValue)));
    }

    private static Optional<String> getOptionalProperty(String property) {
        return Optional.of(System.getProperty(property));
    }

    public String getSensuHost() {
        return sensuHost;
    }

    public int getSensuPort() {
        return sensuPort;
    }

    public String getApplication() {
        return application;
    }

    public String getHostname() {
        return hostname;
    }

    public String getEnvironment() {
        return environment;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getBatchesPerSecond() {
        return batchesPerSecond;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public MetricsConfig withConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
    public MetricsConfig withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    public MetricsConfig withBatchesPerSecond(int batchesPerSecond) {
        this.batchesPerSecond = batchesPerSecond;
        return this;
    }
    public MetricsConfig withQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }
    public MetricsConfig withRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public static class MetricsConfigBuilder {

        private String sensuHost;
        private int sensuPort;
        private String application;
        private String hostname;
        private String environment;
        private int retryInterval;
        private int queueSize;
        private int batchesPerSecond;
        private int batchSize;
        private int connectTimeout;

        public MetricsConfigBuilder() {
        }

        MetricsConfigBuilder(String sensuHost, int sensuPort, String application, String hostname, String environment, int retryInterval, int queueSize, int batchesPerSecond, int batchSize, int connectTimeout) {
            this.sensuHost = sensuHost;
            this.sensuPort = sensuPort;
            this.application = application;
            this.hostname = hostname;
            this.environment = environment;
            this.retryInterval = retryInterval;
            this.queueSize = queueSize;
            this.batchesPerSecond = batchesPerSecond;
            this.batchSize = batchSize;
            this.connectTimeout = connectTimeout;
        }

        public MetricsConfigBuilder sensuHost(String sensuHost){
            this.sensuHost = sensuHost;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder sensuPort(int sensuPort){
            this.sensuPort = sensuPort;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder application(String application){
            this.application = application;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder hostname(String hostname){
            this.hostname = hostname;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder environment(String environment){
            this.environment = environment;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder retryInterval(int retryInterval){
            this.retryInterval = retryInterval;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder queueSize(int queueSize){
            this.queueSize = queueSize;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder batchesPerSecond(int batchesPerSecond){
            this.batchesPerSecond = batchesPerSecond;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder batchSize(int batchSize){
            this.batchSize = batchSize;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfigBuilder connectTimeout(int connectTimeout){
            this.connectTimeout = connectTimeout;
            return MetricsConfigBuilder.this;
        }

        public MetricsConfig build() {
            return new MetricsConfig(this);
        }
    }

    private MetricsConfig(MetricsConfigBuilder builder) {
        this.sensuHost = builder.sensuHost;
        this.sensuPort = builder.sensuPort;
        this.application = builder.application;
        this.hostname = builder.hostname;
        this.environment = builder.environment;
        this.retryInterval = builder.retryInterval;
        this.queueSize = builder.queueSize;
        this.batchesPerSecond = builder.batchesPerSecond;
        this.batchSize = builder.batchSize;
        this.connectTimeout = builder.connectTimeout;
    }
}
