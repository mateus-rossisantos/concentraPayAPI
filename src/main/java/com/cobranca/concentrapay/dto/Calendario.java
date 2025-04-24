package com.cobranca.concentrapay.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Calendario {
    private Date criacao;
    private int expiracao;
}