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
import com.cobranca.concentrapay.exception.NotFoundException;
import com.cobranca.concentrapay.repository.FirebaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;

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
            JSONObject response = efi.call("pixCreateImmediateCharge", new HashMap<>(), body);

            ObjectMapper mapper = new ObjectMapper();
            PixPaymentResponse pixPaymentResponse = mapper.readValue(response.toString(), PixPaymentResponse.class);
            createTxId(pixPaymentResponse);
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

    public PixPaymentResponse getPixInfo(String txid) {
        DocumentSnapshot pedido = firebaseRepository.findPedidoByTxId(txid);

        if (pedido != null) {
            String status = pedido.getString("status");
            if (status != null && status.equals("CLOSED")) {
                return PixPaymentResponse.builder().status("CONCLUIDA").build();
            }
        }
        return PixPaymentResponse.builder().status("EM ABERTO").build();
    }

    public PixSentResponse sendPixPayment(PixSentRequest request, String ecId) {
        JSONObject options = getOptionsFromCredentials();

        HashMap<String, String> params = new HashMap<>();
        params.put("idEnvio", String.valueOf(Math.random() * 10).replace(".", ""));

        JSONObject body = new JSONObject();
        body.put("valor", request.getValor());
        body.put("pagador", new JSONObject().put("chave", options.get("chave")));
        body.put("favorecido", new JSONObject().put("chave", request.getChave()));

        try {
            EfiPay efi= new EfiPay(options);
            JSONObject response = efi.call("pixSend", params, body);

            ObjectMapper mapper = new ObjectMapper();

            PixSentResponse pixSentResponse = mapper.readValue(response.toString(), PixSentResponse.class);
            createE2EId(pixSentResponse, ecId);
            return  pixSentResponse;
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

    public PixSentInfoResponse getSentPixInfo(String id, String e2eId) {
        JSONObject options = getOptionsFromCredentials();

        HashMap<String, String> params = new HashMap<>();
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

    public void clearPendingPayment(String e2eId) {
        firebaseRepository.clearPendingPaymentForEc(e2eId);
    }

    public MoneyPaymentResponse createMoneyPayment(MoneyPaymentRequest request) {
        return firebaseRepository.processMoneyPaymentForEc(request);
    }

    public String processPendingPayments(String ecId) {
        DocumentSnapshot doc = firebaseRepository.findEstablishmentById(ecId);

        if (doc == null || !doc.exists()) {
            log.warn("Estabelecimento com ID {} não encontrado.", ecId);
            throw new NotFoundException(String.format("Estabelecimento com ID {} não encontrado.", ecId));
        }

        Double pendingPayment = doc.getDouble("pendingPayment");
        String chavePix = doc.getString("chavePix");

        if (pendingPayment == null || pendingPayment <= 0.0) {
            log.warn("Estabelecimento {} sem pagamento pendente.", ecId);
            throw new BadRequestException(String.format("Estabelecimento {} sem pagamento pendente.", ecId));
        }

        PixSentRequest pixSentRequest = PixSentRequest.builder()
                .chave(chavePix)
                .valor(String.format(Locale.US, "%.2f", pendingPayment))
                .build();

        PixSentResponse response = this.sendPixPayment(pixSentRequest, ecId);
        log.info("Pagamento enviado para EC {} no valor de R$ {}. Aguardando confirmação...", ecId, pendingPayment);
        return response.getE2eId();
    }

    public void endOrderPayment(String txId) {
        firebaseRepository.addPendingPaymentToEc(txId);

        firebaseRepository.closeCommand(txId);
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

    private void createTxId(PixPaymentResponse pixPaymentResponse) {
        String msg = pixPaymentResponse.getSolicitacaoPagador();
        String comandaId = msg.substring(msg.indexOf("#") + 1).trim();
        firebaseRepository.createTxId(pixPaymentResponse.getTxid(), comandaId);
    }

    private void createE2EId(PixSentResponse pixSentResponse, String ecId) {

        firebaseRepository.createE2EId(pixSentResponse.getE2eId(), ecId);
    }
}
