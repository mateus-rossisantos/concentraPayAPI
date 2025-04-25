package com.cobranca.concentrapay.dto;

import lombok.Data;
import java.util.List;

@Data
public class Pix {
    private String endToEndId;
    private String txid;
    private String valor;
    private String horario;
    private String infoPagador;
    private String chave;
    private List<Devolucao> devolucoes;
}
