package io.github.agorohovcom.eonet.actor;

import java.time.Duration;

public class ActorSystemConfig {

    private final int threadPoolSize;
    private final Duration shutdownTimeout;
    private final Duration pollingInterval;
    private final Duration applicationRunTime;

    public ActorSystemConfig() {
        this.threadPoolSize = Runtime.getRuntime().availableProcessors();
        this.shutdownTimeout = Duration.ofSeconds(5);
        this.pollingInterval = Duration.ofSeconds(10);
        this.applicationRunTime = Duration.ofMinutes(1);
    }

    public ActorSystemConfig(
            int threadPoolSize,
            Duration shutdownTimeout,
            Duration pollingInterval,
            Duration applicationRunTime
    ) {
        this.threadPoolSize = threadPoolSize;
        this.shutdownTimeout = shutdownTimeout;
        this.pollingInterval = pollingInterval;
        this.applicationRunTime = applicationRunTime;
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
}
