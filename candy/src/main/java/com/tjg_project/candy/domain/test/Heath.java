package com.tjg_project.candy.domain.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Heath {

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
