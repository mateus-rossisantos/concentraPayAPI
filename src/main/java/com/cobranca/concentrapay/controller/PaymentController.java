package com.cobranca.concentrapay.controller;

import com.cobranca.concentrapay.dto.request.MoneyPaymentRequest;
import com.cobranca.concentrapay.dto.request.PixPaymentRequest;
import com.cobranca.concentrapay.dto.response.MoneyPaymentResponse;
import com.cobranca.concentrapay.dto.response.PixPaymentResponse;
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


}
