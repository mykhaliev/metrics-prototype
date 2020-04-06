package com.mykhaliev.metrics.client.config;

import com.mykhaliev.metrics.micrometer.MetricConfig;
import com.mykhaliev.metrics.micrometer.MetricRegistry;
import io.micrometer.core.instrument.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricClientConfig {

    private static final String HOSTNAME = "METRIC_CLIENT_LOCALHOST";

    @Bean
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry(new MetricConfig(HOSTNAME), Clock.SYSTEM);
    }

}
