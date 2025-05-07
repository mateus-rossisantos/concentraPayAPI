package com.cobranca.concentrapay.service;

import br.com.efi.efisdk.EfiPay;
import br.com.efi.efisdk.exceptions.EfiPayException;
import com.cobranca.concentrapay.Credentials;
import com.cobranca.concentrapay.dto.request.MoneyPaymentRequest;
import com.cobranca.concentrapay.dto.request.PixPaymentRequest;
import com.cobranca.concentrapay.dto.request.PixSentRequest;
import com.cobranca.concentrapay.dto.response.MoneyPaymentResponse;
import com.cobranca.concentrapay.dto.response.PixPaymentResponse;
import com.cobranca.concentrapay.dto.response.PixSentInfoResponse;
import com.cobranca.concentrapay.dto.response.PixSentResponse;
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

    public PixSentResponse sendPixPayment(PixSentRequest request) {
        JSONObject options = getOptionsFromCredentials();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("idEnvio", "12457567890183473799");

        JSONObject body = new JSONObject();
        body.put("valor", request.getValor());
        body.put("pagador", new JSONObject().put("chave", options.get("chave")));
        body.put("favorecido", new JSONObject().put("chave", request.getChave()));

        try {
            EfiPay efi= new EfiPay(options);
            JSONObject response = efi.call("pixSend", params, body);

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(response.toString(), PixSentResponse.class);
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

    public PixSentInfoResponse getSentPixInfo(String e2eId) {
        JSONObject options = getOptionsFromCredentials();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("e2eId", e2eId);

        try {
            EfiPay efi= new EfiPay(options);
            JSONObject response = efi.call("pixSendDetail", params, new JSONObject());

            ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(response.toString(), PixSentInfoResponse.class);
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

                firebaseRepository.addPendingPaymentToEc(comandaId);

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

    public MoneyPaymentResponse createMoneyPayment(MoneyPaymentRequest request) {
        String ecId = request.getEc();
        double valor = request.getValor();

        if (ecId == null || ecId.isEmpty() || valor <= 0) {
            MoneyPaymentResponse response = new MoneyPaymentResponse();
            response.setEc(ecId != null ? ecId : "desconhecido");
            response.setPendingPayment(0.0);
            response.setAdvancePayment(0.0);
            return response;
        }

        return firebaseRepository.processMoneyPaymentForEc(ecId, valor);
    }

}
