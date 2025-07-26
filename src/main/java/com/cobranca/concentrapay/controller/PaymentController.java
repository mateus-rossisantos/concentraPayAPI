package com.cobranca.concentrapay.controller;

import com.cobranca.concentrapay.dto.request.MoneyPaymentRequest;
import com.cobranca.concentrapay.dto.request.PixPaymentRequest;
import com.cobranca.concentrapay.dto.response.MoneyPaymentResponse;
import com.cobranca.concentrapay.dto.response.PixPaymentResponse;
import com.cobranca.concentrapay.dto.response.PixSentInfoResponse;
import com.cobranca.concentrapay.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pix")
    public PixPaymentResponse createPixPayment(@RequestBody PixPaymentRequest request) {
        log.info("INPUT - createPixPayment - {}", request.getInfoAdicionais());
        return paymentService.createPixPayment(request);
    }

    @GetMapping("/pix/{txid}")
    public PixPaymentResponse getPixInfo(@PathVariable String txid) {
        log.info("INPUT - getPixInfo - {}", txid);
        return paymentService.getPixInfo(txid);
    }

    @PostMapping("/money")
    public ResponseEntity<MoneyPaymentResponse> createMoneyPayment(@RequestBody MoneyPaymentRequest request) {
        log.info("INPUT - createMoneyPayment - EC {} - valor {}", request.getEc(), request.getValor());
        MoneyPaymentResponse response = paymentService.createMoneyPayment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/pending")
    public ResponseEntity<String> processPendingPayments(@PathVariable String id) {
        log.info("INPUT - processPendingPayment - EC {}", id);
        String response = paymentService.processPendingPayments(id);
        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/pending/{e2eId}")
    public ResponseEntity<PixSentInfoResponse> consultPendingPayments(@PathVariable String id, @PathVariable String e2eId) {
        log.info("INPUT - consultPendingPayments - EC {} - e2eId {}", id, e2eId);
        PixSentInfoResponse response = paymentService.getSentPixInfo(id, e2eId);
        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

}
