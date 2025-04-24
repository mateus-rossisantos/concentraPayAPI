package com.cobranca.concentrapay.controller;

import com.cobranca.concentrapay.dto.PixPaymentRequest;
import com.cobranca.concentrapay.dto.PixPaymentResponse;
import com.cobranca.concentrapay.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pix")
    public PixPaymentResponse payWithPix(@RequestBody PixPaymentRequest request) {
        return paymentService.processPixPayment(request);
    }


}
