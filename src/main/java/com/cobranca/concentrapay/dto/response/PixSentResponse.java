package com.cobranca.concentrapay.dto.response;

import com.cobranca.concentrapay.dto.HorarioDevolucao;
import lombok.Data;

@Data
public class PixSentResponse {
    private String idEnvio;
    private String e2eId;
    private String valor;
    private String status;
    private HorarioDevolucao horario;
}
