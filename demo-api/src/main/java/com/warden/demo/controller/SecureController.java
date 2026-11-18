package com.warden.demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class SecureController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
                "service", "demo-api",
                "status", "ok",
                "scope", "secure"
        );
    }
}