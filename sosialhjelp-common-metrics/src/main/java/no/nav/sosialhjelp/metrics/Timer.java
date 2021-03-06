package no.nav.sosialhjelp.metrics;

import java.util.HashMap;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class Timer extends Metric<Timer> {

    private final Timing timing;

    /*
        Bruker både measureTimestamp og startTime fordi System.nanoTime()
        skal brukes for tidsmåling og System.currentTimeMillis() for å
        rapportere når målingen ble gjort.
     */
    private long measureTimestamp;
    private long startTime;
    private long stopTime;

    Timer(MetricsClient metricsClient, String name, Timing timing) {
        super(metricsClient, name + ".timer");
        this.timing = timing;
    }

    public Timer start() {
        measureTimestamp = timing.currentTimeMillis();
        startTime = timing.nanoTime();
        return this;
    }

    public Timer stop() {
        stopTime = timing.nanoTime();
        addFieldToReport("value", getElpasedTimeInMillis());
        return this;
    }

    long getElpasedTimeInMillis() {
        long elapsedTimeNanos = stopTime - startTime;

        return NANOSECONDS.toMillis(elapsedTimeNanos);
    }

    @Override
    protected Timer self() {
        return this;
    }

    @Override
    public Timer report() {
        ensureTimerIsStopped();
        metricsClient.report(name, fields, tags, measureTimestamp);
        reset();
        return this;
    }

    private void ensureTimerIsStopped() {
        if (!fields.containsKey("value")) {
            throw new IllegalStateException("Must stop timer before reporting!");
        }
    }

    /**
     * Timer er ikke threadsafe, bruk en ny timer heller enn å resette en eksisterende
     * om flere tråder kan aksessere målepunktet samtidig
     */
    private void reset() {
        measureTimestamp = 0;
        startTime = 0;
        stopTime = 0;
        fields = new HashMap<>();
    }
}
