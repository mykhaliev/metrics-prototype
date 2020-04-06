package com.mykhaliev.metrics.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mykhaliev.metrics.micrometer.entities.Metric;
import com.mykhaliev.metrics.micrometer.entities.MetricSerie;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/metrics")
public class MetricApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricApiController.class);

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private static final int BATCH_SIZE = 5;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostMapping
    public ResponseEntity createMetrics(@RequestBody MetricSerie body) {
        if (body != null && body.getMetrics() != null) {
            batchUpdateUsingJDBCTemplate(body.getMetrics());
        }
        return ResponseEntity.ok("Metric was created");
    }

    public void batchUpdateUsingJDBCTemplate(final List<Metric> metrics) {
        jdbcTemplate.batchUpdate("INSERT INTO METRICS_VIEW (TIMESTAMP, VALUE, NAME, DIMENSIONS, METADATA)" +
                " VALUES (?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {

            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setObject(1, metrics.get(i).getTimestamp());
                ps.setDouble(2, metrics.get(i).getValue());
                ps.setString(3, metrics.get(i).getName());
                PGobject dimensionJson = new PGobject();
                dimensionJson.setType("json");
                if (metrics.get(i).getDimension() == null) {
                    dimensionJson.setType("{}");
                } else {
                    try {
                        dimensionJson.setValue(MAPPER.writeValueAsString(metrics.get(i).getDimension()));
                    } catch (JsonProcessingException e) {
                        dimensionJson.setValue("{}");
                        LOGGER.error("Failed to serialize dimension, writing {}.");
                    }
                }
                ps.setObject(4, dimensionJson);
                if (metrics.get(i).getMetadata() == null) {
                    ps.setObject(5, null);
                } else {
                    PGobject metaJson = new PGobject();
                    metaJson.setType("json");
                    try {
                        metaJson.setValue(MAPPER.writeValueAsString(metrics.get(i).getMetadata()));
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Failed to serialize metadata, not a valid json. Writing null.");
                    }
                    ps.setObject(5, metaJson);
                }
            }

            @Override
            public int getBatchSize() {
                return BATCH_SIZE;
            }
        });
    }
}
