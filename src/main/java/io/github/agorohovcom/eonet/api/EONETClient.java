package io.github.agorohovcom.eonet.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.agorohovcom.eonet.model.EONETResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

public class EONETClient {
    private static final Logger log = LoggerFactory.getLogger(EONETClient.class);

    private static final String EONET_API_BASE = "https://eonet.gsfc.nasa.gov/api/v3";
    private static final String EVENTS_ENDPOINT = EONET_API_BASE + "/events";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EONETClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Открытые события за последние 30 дней
     */
    public EONETResponse fetchEvents() throws Exception {
        return fetchEvents(30, "open");
    }

    public EONETResponse fetchEvents(int days, String status) throws Exception {
        String url = String.format("%s?days=%d&status=%s", EVENTS_ENDPOINT, days, status);
        log.debug("Fetching EONET events from: {}", url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, EONETResponse.class);
        }
    }

    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
}