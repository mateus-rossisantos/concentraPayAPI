package com.cobranca.concentrapay.controller;

import com.cobranca.concentrapay.dto.request.MoneyPaymentRequest;
import com.cobranca.concentrapay.dto.request.PixPaymentRequest;
import com.cobranca.concentrapay.dto.response.MoneyPaymentResponse;
import com.cobranca.concentrapay.dto.response.PixPaymentResponse;
import com.cobranca.concentrapay.dto.response.PixSentInfoResponse;
import com.cobranca.concentrapay.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pix")
    public PixPaymentResponse createPixPayment(@RequestBody PixPaymentRequest request) {
        return paymentService.createPixPayment(request);
    }

    @GetMapping("/pix/{txid}")
    public PixPaymentResponse getPixInfo(@PathVariable String txid) {
        return paymentService.getPixInfo(txid);
    }

    @PostMapping("/money")
    public ResponseEntity<MoneyPaymentResponse> createMoneyPayment(@RequestBody MoneyPaymentRequest request) {
        MoneyPaymentResponse response = paymentService.createMoneyPayment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/pending")
    public ResponseEntity<String> processPendingPayments(@PathVariable String id) {
        String response = paymentService.processPendingPayments(id);
        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/pending/{e2eId}")
    public ResponseEntity<PixSentInfoResponse> consultPendingPayments(@PathVariable String e2eId) {
        PixSentInfoResponse response = paymentService.getSentPixInfo(e2eId);
        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

}
