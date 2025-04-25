package com.cobranca.concentrapay.service;

import br.com.efi.efisdk.EfiPay;
import br.com.efi.efisdk.exceptions.EfiPayException;
import com.cobranca.concentrapay.Credentials;
import com.cobranca.concentrapay.dto.PixPaymentRequest;
import com.cobranca.concentrapay.dto.PixPaymentResponse;
import com.cobranca.concentrapay.exception.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class PaymentService {

    public PixPaymentResponse createPixPayment(PixPaymentRequest request) {
        Credentials credentials = new Credentials();

        JSONObject options = new JSONObject();
        options.put("client_id", credentials.getClientId());
        options.put("client_secret", credentials.getClientSecret());
        options.put("certificate", credentials.getCertificate());
        options.put("sandbox", credentials.isSandbox());


        JSONObject body = new JSONObject();
        body.put("calendario", new JSONObject().put("expiracao", 3600));
        body.put("valor", new JSONObject().put("original", request.getValor().getOriginal()));
        body.put("chave", credentials.getChave());
        body.put("solicitacaoPagador", request.getSolicitacaoPagador());

        try {
            EfiPay efi = new EfiPay(options);
            JSONObject response = efi.call("pixCreateImmediateCharge", new HashMap<String,String>(), body);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.toString(), PixPaymentResponse.class);
        }catch (EfiPayException e){
            log.error(e.getError());
            log.error(e.getErrorDescription());

            throw new BadRequestException(e.getError() + " " + e.getErrorDescription());
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }
}
