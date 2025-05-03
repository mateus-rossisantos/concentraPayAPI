package com.cobranca.concentrapay.dto.request;

import com.cobranca.concentrapay.dto.Devedor;
import com.cobranca.concentrapay.dto.InfoAdicional;
import com.cobranca.concentrapay.dto.Valor;
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
