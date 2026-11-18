package com.warden.demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partner")
public class PartnerController {

    @GetMapping("/ping")
    public Map<String, String> ping(
            @RequestHeader(name = "X-Warden-Owner", required = false) String owner,
            @RequestHeader(name = "X-Warden-Scopes", required = false) String scopes,
            @RequestHeader(name = "X-Warden-Key-Id", required = false) String keyId) {

        return Map.of(
                "service", "demo-api",
                "status", "ok",
                "scope", "partner",
                "owner", owner == null ? "unknown" : owner,
                "scopes", scopes == null ? "" : scopes,
                "keyId", keyId == null ? "unknown" : keyId
        );
    }
}