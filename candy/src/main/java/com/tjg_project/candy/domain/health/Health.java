package com.tjg_project.candy.domain.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/qwe")
public class Health {

    @GetMapping("/heal")
    public Map<String, String> health() {
        return  Map.of(
                "status", "ok!!"
        );
    }
}
