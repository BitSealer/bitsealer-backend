package com.bitsealer.service;

import com.bitsealer.dto.StamperStartRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StamperClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StamperClient(
            RestTemplate restTemplate,
            @Value("${stamper.base-url:http://localhost:8000}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void startStamp(Long stampId, String sha256) {
        String url = baseUrl + "/stamp";

        StamperStartRequest req = new StamperStartRequest(stampId, sha256);
        ResponseEntity<String> resp =
                restTemplate.postForEntity(url, req, String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    "Stamper respondió " + resp.getStatusCode()
            );
        }
    }
}
