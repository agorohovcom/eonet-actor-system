package io.github.agorohovcom.eonet.actor;

public class PollingSchedulerActor extends MethodHandleActor {
    private final ActorSystem system;
    private final String pollerActor;

    private boolean running = false;
    private int scheduledPolls = 0;

    public PollingSchedulerActor(ActorSystem system, String pollerActor) {
        this.system = system;
        this.pollerActor = pollerActor;
    }

    @Handle
    public void handleStartPolling(StartPolling message) {
        startPeriodicPolling();
    }

    @Handle
    public void handleStopPolling(StopPolling message) {
        stopPeriodicPolling();
    }

    private void startPeriodicPolling() {
        if (!running) {
            running = true;
            scheduledPolls = 0;
            scheduleNextPoll();
        }
    }

    private void stopPeriodicPolling() {
        running = false;
    }

    private void scheduleNextPoll() {
        if (!running) return;

        scheduledPolls++;

        new Thread(() -> {
            try {
                Thread.sleep(10000);
                if (running) {
                    system.sendMessage(pollerActor, new StartPolling());
                    scheduleNextPoll();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
