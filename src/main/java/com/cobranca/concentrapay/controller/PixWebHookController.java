package com.cobranca.concentrapay.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class PixWebHookController {

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> handlePixWebhook() {
        log.info("Webhook acessada");
        return ResponseEntity.ok("200");
    }
}
