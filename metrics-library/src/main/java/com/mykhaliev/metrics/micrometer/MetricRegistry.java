
package com.mykhaliev.metrics.micrometer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.util.MeterPartition;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.micrometer.core.ipc.http.HttpSender;
import io.micrometer.core.ipc.http.HttpUrlConnectionSender;
import io.micrometer.core.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.micrometer.core.instrument.util.StringEscapeUtils.escapeJson;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;


/**
 * Test metric registry.
 */
public class MetricRegistry extends StepMeterRegistry {

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new NamedThreadFactory("test-metrics-publisher");
    private final Logger logger = LoggerFactory.getLogger(MetricRegistry.class);
    private final MetricConfig config;
    private final HttpSender httpClient;


    public MetricRegistry(MetricConfig config, Clock clock) {
        this(config, clock, DEFAULT_THREAD_FACTORY,
                new HttpUrlConnectionSender(config.getServerTimeout(), config.getReadTimeout()));
    }

    public MetricRegistry(MetricConfig config, Clock clock, ThreadFactory threadFactory, HttpSender httpClient) {
        super(config, clock);
        this.config = config;
        this.httpClient = httpClient;
        start(threadFactory);
    }

    @Override
    public void start(ThreadFactory threadFactory) {
        super.start(threadFactory);
    }

    @Override
    protected void publish() {
        String serverEndpoint = config.getServerUrl();
        try {
            for (List<Meter> batch : MeterPartition.partition(this, config.batchSize())) {
                String body = batch.stream().flatMap(meter -> meter.match(
                        m -> writeMeter(m), // visitGauge
                        m -> writeMeter(m), // visitCounter
                        timer -> writeTimer(timer), // visitTimer
                        summary -> writeSummary(summary), // visitSummary
                        m -> writeMeter(m), // visitLongTaskTimer
                        m -> writeMeter(m), // visitTimeGauge
                        m -> writeMeter(m), // visitFunctionCounter
                        timer -> writeFunctionTimer(timer), // visitFunctionTimer
                        m -> writeMeter(m)) // visitMeter
                ).collect(joining(",", "{\"metrics\":[", "]}"));

                logger.trace("sending metrics batch to server:{}{}", System.lineSeparator(), body);

                httpClient.post(serverEndpoint)
                        .withJsonContent(
                                body)
                        .send()
                        .onSuccess(response -> logger.debug("successfully sent {} metrics to server", batch.size()))
                        .onError(response -> logger.error("failed to send metrics to server: {}", response.body()));
            }
        } catch (Throwable e) {
            logger.warn("failed to send metrics to server", e);
        }
    }

    private Stream<String> writeFunctionTimer(FunctionTimer timer) {
        long wallTime = clock.wallTime();

        Meter.Id id = timer.getId();
        // we can't know anything about max and percentiles originating from a function timer
        return Stream.of(
                writeMetric(id, "count", wallTime, timer.count()),
                writeMetric(id, "avg", wallTime, timer.mean(getBaseTimeUnit())),
                writeMetric(id, "sum", wallTime, timer.totalTime(getBaseTimeUnit())));
    }

    private Stream<String> writeTimer(Timer timer) {
        final long wallTime = clock.wallTime();
        final Stream.Builder<String> metrics = Stream.builder();

        Meter.Id id = timer.getId();
        metrics.add(writeMetric(id, "sum", wallTime, timer.totalTime(getBaseTimeUnit())));
        metrics.add(writeMetric(id, "count", wallTime, timer.count()));
        metrics.add(writeMetric(id, "avg", wallTime, timer.mean(getBaseTimeUnit())));
        metrics.add(writeMetric(id, "max", wallTime, timer.max(getBaseTimeUnit())));

        return metrics.build();
    }

    private Stream<String> writeSummary(DistributionSummary summary) {
        final long wallTime = clock.wallTime();
        final Stream.Builder<String> metrics = Stream.builder();

        Meter.Id id = summary.getId();
        metrics.add(writeMetric(id, "sum", wallTime, summary.totalAmount()));
        metrics.add(writeMetric(id, "count", wallTime, summary.count()));
        metrics.add(writeMetric(id, "avg", wallTime, summary.mean()));
        metrics.add(writeMetric(id, "max", wallTime, summary.max()));

        return metrics.build();
    }

    private Stream<String> writeMeter(Meter m) {
        long wallTime = clock.wallTime();
        return stream(m.measure().spliterator(), false)
                .map(ms -> {
                    Meter.Id id = m.getId().withTag(ms.getStatistic());
                    return writeMetric(id, null, wallTime, ms.getValue());
                });
    }


    private String writeMetric(Meter.Id id, @Nullable String suffix, long wallTime, double value) {
        Meter.Id fullId = id;
        if (suffix != null) {
            fullId = idWithSuffix(id, suffix);
        }
        Iterable<Tag> tags = getConventionTags(fullId);
        //metadata
        String metadata = tags.iterator().hasNext()
                ? stream(tags.spliterator(), false)
                .map(t -> "\"" + escapeJson(t.getKey()) + ":" + escapeJson(t.getValue()) + "\"")
                .collect(joining(",", ",\"metadata\":{ \"tags\":[", "]}"))
                : "";
        StringBuilder metricBuilder = new StringBuilder();
        metricBuilder.
                append("{");
        metricBuilder.append("\"name\" :\"");
        metricBuilder.append(id.getName());
        metricBuilder.append("\",\"value\": ");
        metricBuilder.append(value);
        metricBuilder.append(",\"timestamp\": ");
        metricBuilder.append(wallTime);
        metricBuilder.append(",\"dimension\": ");
        metricBuilder.append(config.getDimension());
        metricBuilder.append(metadata);
        metricBuilder.append("}");
        return metricBuilder.toString();
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    private Meter.Id idWithSuffix(Meter.Id id, String suffix) {
        return id.withName(id.getName() + "." + suffix);
    }

}