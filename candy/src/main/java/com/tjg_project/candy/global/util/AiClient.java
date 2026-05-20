package com.tjg_project.candy.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public Map<String, Object> getCollabProducts(
            Long userId
    ) {

        String url =
                aiServerUrl +
                        "/collab/" +
                        userId;

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        url,
                        Map.class
                );

        Map<String, Object> body =
                response.getBody();

        return body;
    }


    public Map<String, Object> getitemBasedProducts(
            Long productId
    ) {

        String url =
                aiServerUrl +
                        "/item-based/" +
                        productId;

        ResponseEntity<Map> response =
                restTemplate.getForEntity(
                        url,
                        Map.class
                );

        Map<String, Object> body =
                response.getBody();

        return body;
    }


}