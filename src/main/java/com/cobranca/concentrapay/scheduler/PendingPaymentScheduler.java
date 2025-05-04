package com.cobranca.concentrapay.scheduler;

import com.cobranca.concentrapay.service.ConsultingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PendingPaymentScheduler {

    @Autowired
    private ConsultingService consultingService;

    // Executa a cada dia às 02:00 da manhã
    @Scheduled(cron = "0 0 2 * * *")
    public void schedulePendingPaymentsProcessing() {
        log.info("Iniciando processamento automático de pagamentos pendentes...");
        consultingService.processPendingPayments();
        log.info("Processamento automático concluído.");
    }
}
