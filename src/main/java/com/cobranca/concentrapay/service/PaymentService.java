package com.cobranca.concentrapay.service;

import br.com.efi.efisdk.EfiPay;
import br.com.efi.efisdk.exceptions.EfiPayException;
import com.cobranca.concentrapay.Credentials;
import com.cobranca.concentrapay.dto.PixPaymentRequest;
import com.cobranca.concentrapay.dto.PixPaymentResponse;
import com.cobranca.concentrapay.exception.BadRequestException;
import com.cobranca.concentrapay.repository.FirebaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private FirebaseRepository firebaseRepository;

    public PixPaymentResponse createPixPayment(PixPaymentRequest request) {
        JSONObject options = getOptionsFromCredentials();

        JSONObject body = new JSONObject();
        body.put("calendario", new JSONObject().put("expiracao", 3600));
        body.put("valor", new JSONObject().put("original", request.getValor().getOriginal()));
        body.put("chave", options.get("chave"));
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

    public PixPaymentResponse getPixInfo(String txid) {
        JSONObject options = getOptionsFromCredentials();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("txid", txid);

        try {
            EfiPay efi = new EfiPay(options);
            JSONObject response = efi.call("pixDetailCharge", params, new JSONObject());

            ObjectMapper mapper = new ObjectMapper();
            PixPaymentResponse pixPaymentResponse = mapper.readValue(response.toString(), PixPaymentResponse.class);

            checkOrderPayment(pixPaymentResponse);
            return pixPaymentResponse;
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

    private void checkOrderPayment(PixPaymentResponse pixPaymentResponse) {
        if ("CONCLUIDA".equals(pixPaymentResponse.getStatus())) {
            try {
                String solicitacao = pixPaymentResponse.getSolicitacaoPagador();
                String comandaId = solicitacao.substring(solicitacao.indexOf("#") + 1).trim();

                firebaseRepository.updateOrdersByCommandNumber(comandaId, "CLOSED");
            } catch (Exception e) {
                log.error("Erro ao processar pagamento da comanda: " + e.getMessage(), e);
            }
        }
    }

    private JSONObject getOptionsFromCredentials() {
        Credentials credentials = new Credentials();

        JSONObject options = new JSONObject();
        options.put("client_id", credentials.getClientId());
        options.put("client_secret", credentials.getClientSecret());
        options.put("certificate", credentials.getCertificate());
        options.put("sandbox", credentials.isSandbox());
        options.put("chave", credentials.getChave());

        return options;
    }
}
