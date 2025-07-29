package com.cobranca.concentrapay.dto.response;

import com.cobranca.concentrapay.dto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
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
