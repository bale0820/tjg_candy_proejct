package com.tjg_project.candy.domain.ai.service;


import com.tjg_project.candy.global.util.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiClient aiClient;

    public Map<String, Object> getRecommendProductsbyusers(
            Long userId
    ) {

        return aiClient.getCollabProducts(
                userId
        );

    }

    public Map<String, Object> getRecommendProductsbyproducts(
            Long userId
    ) {

        return aiClient.getitemBasedProducts(
                userId
        );

    }

}