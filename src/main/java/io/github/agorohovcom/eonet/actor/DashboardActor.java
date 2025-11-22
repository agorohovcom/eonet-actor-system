package io.github.agorohovcom.eonet.actor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardActor implements Actor {
    private StatisticsUpdate lastStats;
    private int updateCount = 0;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void onMessage(Object message) {
        if (message instanceof StatisticsUpdate stats) {
            this.lastStats = stats;
            this.updateCount++;
            displayDashboard(stats);
        }
    }

    private void displayDashboard(StatisticsUpdate stats) {
        System.out.println("\n=== EONET EVENT STATISTICS ===");
        System.out.println("Update #" + updateCount + " at " + LocalDateTime.now().format(timeFormatter));
        System.out.println();

        if (lastStats != null) {
            System.out.println("Total events: " + lastStats.totalEvents());
            System.out.println();
            System.out.println("Events by category:");

            // Сортируем по количеству (от большего к меньшему)
            lastStats.eventsByCategory().entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .forEach(entry -> {
                        String category = entry.getKey();
                        long count = entry.getValue();
                        int percentage = (int) ((count * 100) / lastStats.totalEvents());
                        String bar = generateProgressBar(percentage, 20);
                        System.out.printf("  %-20s: %3d %s %d%%%n", category, count, bar, percentage);
                    });
        }

        System.out.println();
        System.out.println("Data updates every 10 seconds");
        System.out.println("==============================");
    }

    private String generateProgressBar(int percentage, int length) {
        int bars = (percentage * length) / 100;

        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < length; i++) {
            if (i < bars) {
                bar.append("#");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");
        return bar.toString();
    }
}
