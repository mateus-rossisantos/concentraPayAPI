package com.cobranca.concentrapay.service;

import com.cobranca.concentrapay.dto.request.PixSentRequest;
import com.cobranca.concentrapay.dto.response.PixSentInfoResponse;
import com.cobranca.concentrapay.dto.response.PixSentResponse;
import com.cobranca.concentrapay.repository.FirebaseRepository;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ConsultingService {

    @Autowired
    private FirebaseRepository firebaseRepository;

    @Autowired
    private PaymentService paymentService;

    public void processPendingPayments() {
        List<QueryDocumentSnapshot> establishments = firebaseRepository.findEstablishmentsWithPendingPayments();

        for (QueryDocumentSnapshot doc : establishments) {
            String ecId = doc.getId();
            Double pendingPayment = doc.getDouble("pendingPayment");
            String chavePix = doc.getString("chavePix");

            if (pendingPayment == null || chavePix == null || chavePix.isEmpty()) {
                log.warn("Estabelecimento {} com dados incompletos. Ignorando...", ecId);
                continue;
            }

            PixSentRequest pixSentRequest = PixSentRequest.builder()
                    .chave(chavePix)
                    .valor(pendingPayment.toString())
                    .build();

            // Envia o pagamento
            PixSentResponse response = paymentService.sendPixPayment(pixSentRequest);
            String e2eId = response.getE2eId();
            log.info("Pagamento enviado para EC {} no valor de R$ {}. Aguardando confirmação...", ecId, pendingPayment);

            // Aguarda o pagamento ser concluído
            while (true) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrompida durante espera de confirmação de Pix.");
                    return;
                }

                PixSentInfoResponse statusResponse = paymentService.getSentPixInfo(e2eId);
                String status = statusResponse.getStatus();

                if ("REALIZADO".equalsIgnoreCase(status)) {
                    log.info("Pagamento para EC {} confirmado com status: {}", ecId, status);
                    firebaseRepository.clearPendingPaymentForEc(ecId);
                    break;
                } else {
                    log.info("Aguardando conclusão do Pix para EC {}. Status atual: {}", ecId, status);
                }
            }
        }
    }

}
