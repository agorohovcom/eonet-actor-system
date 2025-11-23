package io.github.agorohovcom.eonet.actor;

import java.time.Duration;

public class ActorSystemConfig {

    private final int threadPoolSize;
    private final Duration shutdownTimeout;
    private final Duration pollingInterval;
    private final Duration applicationRunTime;
    private final Duration cleanupInterval;
    private final Duration maxIdleTime;


    public ActorSystemConfig() {
        this.threadPoolSize = Runtime.getRuntime().availableProcessors();
        this.shutdownTimeout = Duration.ofSeconds(5);
        this.pollingInterval = Duration.ofSeconds(10);
        this.applicationRunTime = Duration.ofMinutes(2);
        this.cleanupInterval = Duration.ofMinutes(1);
        this.maxIdleTime = Duration.ofMinutes(2);
    }

    public ActorSystemConfig(
            int threadPoolSize,
            Duration shutdownTimeout,
            Duration pollingInterval,
            Duration applicationRunTime,
            Duration cleanupInterval,
            Duration maxIdleTime
    ) {
        this.threadPoolSize = threadPoolSize;
        this.shutdownTimeout = shutdownTimeout;
        this.pollingInterval = pollingInterval;
        this.applicationRunTime = applicationRunTime;
        this.cleanupInterval = cleanupInterval;
        this.maxIdleTime = maxIdleTime;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    public Duration getPollingInterval() {
        return pollingInterval;
    }

    public Duration getApplicationRunTime() {
        return applicationRunTime;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public Duration getMaxIdleTime() {
        return maxIdleTime;
    }
}
