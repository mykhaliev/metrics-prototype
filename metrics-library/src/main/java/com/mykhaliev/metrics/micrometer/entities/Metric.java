package com.mykhaliev.metrics.micrometer.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mykhaliev.metrics.micrometer.entities.json.OffsetDateTimeMillisDeserializer;
import com.mykhaliev.metrics.micrometer.entities.json.OffsetDateTimeMillisSerializer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class Metric {

    @JsonSerialize(using = OffsetDateTimeMillisSerializer.class)
    @JsonDeserialize(using = OffsetDateTimeMillisDeserializer.class)
    private OffsetDateTime timestamp;

    private double value;

    private String name;

    private List<String> tags;

    private Map<String, Object> metadata;

    private Dimension dimension;

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    static class Dimension {

        String hostname;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
    }

}
