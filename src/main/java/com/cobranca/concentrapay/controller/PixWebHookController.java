package com.cobranca.concentrapay.controller;

import com.cobranca.concentrapay.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class PixWebHookController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> handShakePixWebhook() {
        log.info("Webhook acessada");
        return ResponseEntity.ok("200");
    }

    @PostMapping("/pix")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> handlePixWebhook(@RequestBody String response) throws JsonProcessingException {
        log.info("Webhook 2 acessada");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        JsonNode pixNode = root.get("pix").get(0);
        String txid = null;

        if (pixNode.has("txid") && !pixNode.get("txid").isNull()) {
            txid = pixNode.get("txid").asText();
            paymentService.endOrderPayment(txid);
        } else {
            String status = pixNode.get("status").asText();
            String e2eId = pixNode.get("endToEndId").asText();
            if (status.equals("REALIZADO")) {
                paymentService.clearPendingPayment(e2eId);
            }
        }

        return ResponseEntity.ok("200");
    }
}
