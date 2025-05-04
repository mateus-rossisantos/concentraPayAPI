package com.cobranca.concentrapay.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PixSentRequest {
    private String valor;
    private String chave;
}
