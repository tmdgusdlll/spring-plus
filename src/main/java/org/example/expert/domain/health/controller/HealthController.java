package org.example.expert.domain.health.controller;

import org.example.expert.domain.health.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(
                "UP", LocalDateTime.now().toString()
        );
    }
}
