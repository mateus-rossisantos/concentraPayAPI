package com.cobranca.concentrapay.service;

import com.cobranca.concentrapay.dto.request.PixSentRequest;
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

            PixSentRequest pixSentRequest = PixSentRequest.builder().chave(chavePix).valor(pendingPayment.toString()).build();
            paymentService.sendPixPayment(pixSentRequest);
            log.info("Pagamento enviado para EC {} no valor de R$ {}", ecId, pendingPayment);
        }
    }
}
