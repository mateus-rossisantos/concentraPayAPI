package com.cobranca.concentrapay.dto.response;

import com.cobranca.concentrapay.dto.*;
import lombok.Data;

import java.util.List;

@Data
public class PixPaymentResponse {
    private Calendario calendario;
    private String txid;
    private int revisao;
    private Loc loc;
    private String location;
    private String status;
    private Devedor devedor;
    private Valor valor;
    private String chave;
    private String solicitacaoPagador;
    private String pixCopiaECola;
    private List<Pix> pix;
}
