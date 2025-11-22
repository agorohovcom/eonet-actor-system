package io.github.agorohovcom.eonet.actor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardActor extends MethodHandleActor {
    private StatisticsUpdate lastStats;
    private int updateCount = 0;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

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
        System.out.println("\n=== EONET EVENT STATISTICS ===");
        System.out.println("Update #" + updateCount + " at " + LocalDateTime.now().format(timeFormatter));
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
                        String bar = generateProgressBar(percentage, 20);
                        System.out.printf("  %-20s: %3d %s %d%%%n", category, count, bar, percentage);
                    });
        }

        System.out.println();
        System.out.println("Data updates every 10 seconds");
        System.out.println("App will stop in 1 minute");
        System.out.println("==============================");
    }

    private String generateProgressBar(int percentage, int length) {
        int bars = (percentage * length) / 100;
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < length; i++) {
            bar.append(i < bars ? "#" : " ");
        }
        bar.append("]");
        return bar.toString();
    }
}
