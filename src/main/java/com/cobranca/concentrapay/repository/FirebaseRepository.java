package com.cobranca.concentrapay.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
public class FirebaseRepository {

    public void updateOrdersByCommandNumber(String commandNumber, String newStatus) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference orders = db.collection("order");

        ApiFuture<QuerySnapshot> query = orders.whereEqualTo("commandNumber", commandNumber).get();

        try {
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            if (documents.isEmpty()) {
                log.warn("Nenhum pedido encontrado com commandNumber = {}", commandNumber);
                return;
            }

            for (QueryDocumentSnapshot doc : documents) {
                DocumentReference docRef = doc.getReference();
                docRef.update("status", newStatus);
                log.info("Atualizado pedido {} para status {}", docRef.getId(), newStatus);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erro ao atualizar pedidos: {}", e.getMessage(), e);
        }
    }

    public void createPaymentsForCreatedOrders(String commandNumber) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference orders = db.collection("order");

        try {
            ApiFuture<QuerySnapshot> query = orders
                    .whereEqualTo("commandNumber", commandNumber)
                    .whereEqualTo("status", "CREATED")
                    .get();

            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                String ec = doc.getString("ec");
                String data = doc.getString("date");
                Double valor = doc.getDouble("value");

                DocumentReference pagamentoDoc = db.collection("pagamento").document();
                pagamentoDoc.set(new HashMap<>() {{
                    put("id", pagamentoDoc.getId());
                    put("ec", ec);
                    put("date", data);
                    put("value", valor);
                    put("status", "PENDING");
                }});

                log.info("Criado pagamento para pedido {} com ID de pagamento {}", doc.getId(), pagamentoDoc.getId());
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("Erro ao criar pagamentos para comanda {}: {}", commandNumber, e.getMessage(), e);
        }
    }
}
