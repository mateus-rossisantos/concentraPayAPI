package com.cobranca.concentrapay.controller;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class PixWebHookController {

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> handShakePixWebhook() {
        log.info("Webhook acessada");
        return ResponseEntity.ok("200");
    }

    @PostMapping("/pix")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> handlePixWebhook(JSONObject response) {
        log.info("Webhook 2 acessada");
        log.info(response.toString());

        return ResponseEntity.ok("200");
    }
}
