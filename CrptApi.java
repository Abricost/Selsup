package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.*;

public class CrptApi {

    private final String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper;

    private final int requestLimit;
    private final Duration timeInterval;
    private final Semaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeInterval = Duration.ofMillis(timeUnit.toMillis(1) * 1000);
        this.requestLimit = requestLimit;
        this.semaphore = new Semaphore(requestLimit);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void createDocument(String jsonDocument) throws InterruptedException, IOException {
        semaphore.acquire();

        RequestBody body = RequestBody.create(MediaType.get("application/json"), jsonDocument);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                semaphore.release();
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                semaphore.release();
            }
        });
    }
}