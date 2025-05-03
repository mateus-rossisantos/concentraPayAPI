package com.cobranca.concentrapay.dto.request;

import com.cobranca.concentrapay.dto.Valor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PixSentRequest {
    private Valor valor;
    private String chave;
}
