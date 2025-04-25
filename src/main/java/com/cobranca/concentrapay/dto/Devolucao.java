package com.cobranca.concentrapay.dto;

import lombok.Data;

@Data
public class Devolucao {
    private String id;
    private String rtrId;
    private String valor;
    private HorarioDevolucao horario;
    private String status;
}
