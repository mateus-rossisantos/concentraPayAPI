package com.cobranca.concentrapay.dto.response;

import com.cobranca.concentrapay.dto.Horario;
import lombok.Data;

@Data
public class PixSentInfoResponse {
    private Horario horario;
    private String chave;
    private String idEnvio;
    private String endToEndId;
    private String valor;
    private Favorecido favorecido;
    private String status;
}
