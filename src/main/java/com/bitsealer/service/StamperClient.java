package com.bitsealer.service;

import com.bitsealer.dto.StamperStartRequest;
import com.bitsealer.dto.StamperStampResponse;
import com.bitsealer.dto.StamperUpgradeRequest;
import com.bitsealer.dto.StamperUpgradeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Component
public class StamperClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public StamperClient(RestTemplate restTemplate,
                         @Value("${stamper.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * /stamp (stateless): devuelve el proof .ots en Base64.
     */
    public StamperStampResponse stamp(Long stampId, String sha256, String originalFilename, byte[] fileBytes) {
        String url = baseUrl + "/stamp";

        String fileB64 = Base64.getEncoder().encodeToString(fileBytes);
        StamperStartRequest req = new StamperStartRequest(stampId, sha256, originalFilename, fileB64);

        try {
            ResponseEntity<StamperStampResponse> resp = restTemplate.postForEntity(url, req, StamperStampResponse.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("Stamper respondió " + resp.getStatusCode());
            }

            if (resp.getBody().otsProofB64() == null || resp.getBody().otsProofB64().isBlank()) {
                throw new RuntimeException("Stamper devolvió otsProofB64 vacío");
            }

            return resp.getBody();

        } catch (RestClientException e) {
            throw new RuntimeException("No se pudo contactar con stamper en " + url, e);
        }
    }

    /**
     * /upgrade (stateless): recibe proof .ots (Base64) y devuelve proof actualizado.
     */
    public StamperUpgradeResponse upgrade(Long stampId, byte[] otsProofBytes) {
        String url = baseUrl + "/upgrade";

        String otsB64 = Base64.getEncoder().encodeToString(otsProofBytes);
        StamperUpgradeRequest req = new StamperUpgradeRequest(stampId, otsB64);

        try {
            ResponseEntity<StamperUpgradeResponse> resp = restTemplate.postForEntity(url, req, StamperUpgradeResponse.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("Stamper respondió " + resp.getStatusCode());
            }

            if (resp.getBody().otsProofB64() == null || resp.getBody().otsProofB64().isBlank()) {
                throw new RuntimeException("Stamper devolvió otsProofB64 vacío en upgrade");
            }

            return resp.getBody();

        } catch (RestClientException e) {
            throw new RuntimeException("No se pudo contactar con stamper en " + url, e);
        }
    }
}
