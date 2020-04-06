package com.mykhaliev.metrics.micrometer.entities.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class OffsetDateTimeMillisDeserializer extends JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        return Instant.ofEpochMilli(Long.parseLong(p.getText())).atZone(ZoneId.of("UTC"))
                .toOffsetDateTime();
    }
}
