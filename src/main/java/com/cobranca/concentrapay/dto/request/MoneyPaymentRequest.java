package com.cobranca.concentrapay.dto.request;

import lombok.Data;

@Data
public class MoneyPaymentRequest {
    private double valor;
    private String ec;
    private String comanda;
}
