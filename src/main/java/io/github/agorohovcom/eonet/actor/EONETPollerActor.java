package io.github.agorohovcom.eonet.actor;

import io.github.agorohovcom.eonet.api.EONETClient;
import io.github.agorohovcom.eonet.model.EONETResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EONETPollerActor extends MethodHandleActor {
    private static final Logger log = LoggerFactory.getLogger(EONETPollerActor.class);

    private int pollCount = 0;

    private final EONETClient client;
    private final ActorSystem system;
    private final String nextActor;

    public EONETPollerActor(ActorSystem system, String nextActor) {
        this.system = system;
        this.nextActor = nextActor;
        this.client = new EONETClient();
    }

    @Handle
    public void handleStartPolling(StartPolling message) {
        pollCount++;
        log.info("Polling EONET API (attempt #{})", pollCount);

        try {
            EONETResponse response = client.fetchEvents();
            log.info("Successfully fetched {} events", response.events().size());

            system.sendMessage(nextActor, new EventsFetched(response));

        } catch (Exception e) {
            log.error("Failed to fetch events: {}", e.getMessage());
            system.sendMessage(nextActor, new PollingError(e.getMessage()));
        }
    }

    @Handle
    public void handleStopPolling(StopPolling message) {
        log.info("Stopping EONET poller after {} polls", pollCount);
        client.close();
    }

    @Override
    protected void unhandled(Object message) {
        log.warn("EONETPollerActor received unhandled message: {}", message.getClass().getSimpleName());
    }
}
