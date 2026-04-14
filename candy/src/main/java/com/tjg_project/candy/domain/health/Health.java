package com.tjg_project.candy.domain.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class Health {

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
