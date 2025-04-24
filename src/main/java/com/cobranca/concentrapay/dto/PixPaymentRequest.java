package com.cobranca.concentrapay.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PixPaymentRequest {

    private Devedor devedor;
    private Valor valor;
    private String chave;
    private String solicitacaoPagador;
    private List<InfoAdicional> infoAdicionais;
}
