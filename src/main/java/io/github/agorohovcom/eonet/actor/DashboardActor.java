package io.github.agorohovcom.eonet.actor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardActor extends MethodHandleActor {
    private StatisticsUpdate lastStats;
    private int updateCount = 0;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Duration pollingInterval;
    private final Duration applicationRunTime;
    private LocalDateTime startTime;

    public DashboardActor(Duration pollingInterval, Duration applicationRunTime) {
        this.pollingInterval = pollingInterval;
        this.applicationRunTime = applicationRunTime;
        this.startTime = LocalDateTime.now();
    }

    @Handle
    public void handleStatisticsUpdate(StatisticsUpdate message) {
        this.lastStats = message;
        this.updateCount++;
        displayDashboard();
    }

    @Handle
    public void handleProcessingError(ProcessingError message) {
        System.out.println("\n=== EONET ERROR ===");
        System.out.println("Error: " + message.error());
        System.out.println("Time: " + LocalDateTime.now().format(timeFormatter));
        System.out.println("===================");
    }

    private void displayDashboard() {
        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(startTime, now);
        Duration remaining = applicationRunTime.minus(elapsed);

        long secondsRemaining = remaining.getSeconds();
        long minutes = secondsRemaining / 60;
        long seconds = secondsRemaining % 60;

        System.out.println("\n=== EONET EVENT STATISTICS ===");
        System.out.println("Update #" + updateCount + " at " + now.format(timeFormatter));
        System.out.println();

        if (lastStats != null) {
            System.out.println("Total events: " + lastStats.totalEvents());
            System.out.println();
            System.out.println("Events by category:");

            lastStats.eventsByCategory().entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .forEach(entry -> {
                        String category = entry.getKey();
                        long count = entry.getValue();
                        int percentage = lastStats.totalEvents() > 0 ?
                                (int) ((count * 100) / lastStats.totalEvents()) : 0;
                        String bar = generateProgressBar(percentage);
                        System.out.printf("  %-20s: %3d %s %d%%%n", category, count, bar, percentage);
                    });
        }

        System.out.println();
        System.out.println("Data updates every " + pollingInterval.getSeconds() + " seconds");
        System.out.printf("App will stop in %d:%02d%n", minutes, seconds);
        System.out.println("==============================");
    }

    private String generateProgressBar(int percentage) {
        int bars = (percentage * 20) / 100;
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < 20; i++) {
            bar.append(i < bars ? "#" : " ");
        }
        bar.append("]");
        return bar.toString();
    }
}
