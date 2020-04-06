package com.mykhaliev.metrics.micrometer;

import io.micrometer.core.instrument.step.StepRegistryConfig;

import java.time.Duration;

public class MetricConfig implements StepRegistryConfig {

    private final Duration serverTimeout = Duration.ofSeconds(5);
    private final Duration readTimeout = Duration.ofSeconds(5);
    private final Duration step = Duration.ofSeconds(10);

    private String dimension;

    public MetricConfig(String hostname) {
        this.dimension = "{\"hostname\": \"" + hostname + "\"}";
    }

    @Override
    public String prefix() {
        return "TestMetric";
    }

    @Override
    public String get(String key) {
        return null;
    }

    public String getDimension() {
        return dimension;
    }

    public Duration getServerTimeout() {
        return serverTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public String getServerUrl() {
        return "http://localhost:8081/metrics";
    }

    @Override
    public Duration step() {
        return step;
    }
}
