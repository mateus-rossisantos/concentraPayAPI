package com.cobranca.concentrapay.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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
}
