package com.cobranca.concentrapay.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void addPendingPaymentToEc(String commandNumber) {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference orders = db.collection("order");

        try {
            // Buscar os pedidos com a comanda e status CREATED
            ApiFuture<QuerySnapshot> query = orders
                    .whereEqualTo("commandNumber", commandNumber)
                    .whereEqualTo("status", "CREATED")
                    .get();

            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            if (documents.isEmpty()) {
                log.warn("Nenhum pedido com status CREATED para comanda {}", commandNumber);
                return;
            }

            // Mapeia os valores por ec
            Map<String, Double> valoresPorEc = new HashMap<>();

            for (QueryDocumentSnapshot doc : documents) {
                String ec = doc.getString("ec");
                Double valor = doc.getDouble("valor");
                if (ec != null && valor != null) {
                    valoresPorEc.merge(ec, valor, Double::sum);
                }
            }

            // Atualiza o pendingPayment de cada ec
            for (Map.Entry<String, Double> entry : valoresPorEc.entrySet()) {
                String ecId = entry.getKey();
                Double valorTotal = entry.getValue();

                DocumentReference ecRef = db.collection("ec").document(ecId);
                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(ecRef).get();
                    Double pending = snapshot.contains("pendingPayment") ? snapshot.getDouble("pendingPayment") : 0.0;
                    if (pending == null) pending = 0.0;
                    transaction.update(ecRef, "pendingPayment", pending + valorTotal);
                    return null;
                }).get();

                log.info("Atualizado pendingPayment para EC {}: +{}", ecId, valorTotal);
            }

        } catch (InterruptedException | ExecutionException e) {
            log.error("Erro ao atualizar pendingPayment para comanda {}: {}", commandNumber, e.getMessage(), e);
        }
    }

    public void clearPendingPaymentForEc(String ecId) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ecRef = db.collection("estabelecimento").document(ecId);

        try {
            ecRef.update("pendingPayment", 0.0);
            log.info("Zerado pendingPayment para EC {}", ecId);
        } catch (Exception e) {
            log.error("Erro ao zerar pendingPayment para EC {}: {}", ecId, e.getMessage(), e);
        }
    }


    public List<QueryDocumentSnapshot> findEstablishmentsWithPendingPayments() {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference establishments = db.collection("estabelecimento");

        try {
            ApiFuture<QuerySnapshot> query = establishments
                    .whereGreaterThan("pendingPayment", 0.0)
                    .get();

            return query.get().getDocuments();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erro ao buscar estabelecimentos com pagamentos pendentes: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
