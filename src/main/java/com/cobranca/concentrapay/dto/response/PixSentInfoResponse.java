package com.cobranca.concentrapay.dto.response;

import lombok.Data;

@Data
public class PixSentInfoResponse {
    private String idEnvio;
    private String endToEndId;
    private String valor;
    private String status;
}
