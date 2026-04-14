package com.tjg_project.candy.domain.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qwe")
public class Health {

    @GetMapping("/heal")
    public String health() {
        return "ok";
    }
}
