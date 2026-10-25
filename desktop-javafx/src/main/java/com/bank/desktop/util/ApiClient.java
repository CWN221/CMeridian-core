package com.bank.desktop.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ApiClient {

    private static ApiClient instance;
    private final OkHttpClient client;
    @Getter private final String baseUrl;
    @Getter @Setter private String accessToken;

    private ApiClient() {
        this.baseUrl = System.getProperty("api.base.url", "http://localhost:8080");
        
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
        
        log.info("ApiClient initialized with base URL: {}", baseUrl);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public Request.Builder authenticatedRequest() {
        Request.Builder builder = new Request.Builder();
        if (accessToken != null && !accessToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + accessToken);
        }
        return builder;
    }

    public Response executeAuthenticated(Request request) throws IOException {
        Request.Builder builder = request.newBuilder();
        if (accessToken != null && !accessToken.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + accessToken);
        }
        return client.newCall(builder.build()).execute();
    }

    public Response executeRequest(Request request) throws IOException {
        return client.newCall(request).execute();
    }
}