package com.tjg_project.candy.domain.ai.controller;

import com.tjg_project.candy.domain.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    @GetMapping("/recommend/{userId}")
    public ResponseEntity<?> recommendProductsByUsers(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                aiService.getRecommendProductsbyusers(
                        userId
                )
        );

    }


    @GetMapping("/item-based/{productId}")
    public ResponseEntity<?> recommendProductsByProducts(
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                aiService.getRecommendProductsbyproducts(
                        productId
                )
        );

    }

}
