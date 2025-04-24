package com.cobranca.concentrapay.dto;

import lombok.Data;

@Data
public class Devedor {
    private String cnpj;
    private String cpf;
    private String nome;
}