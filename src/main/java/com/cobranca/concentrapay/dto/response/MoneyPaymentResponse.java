package com.cobranca.concentrapay.dto.response;

import lombok.Data;

@Data
public class MoneyPaymentResponse {
    private String ec;
    private double pendingPayment;
    private double advancePayment;
}
