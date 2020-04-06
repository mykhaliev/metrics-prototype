package com.mykhaliev.metrics.micrometer.entities;

import java.util.List;

public class MetricSerie {

    List<Metric> metrics;

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
}
