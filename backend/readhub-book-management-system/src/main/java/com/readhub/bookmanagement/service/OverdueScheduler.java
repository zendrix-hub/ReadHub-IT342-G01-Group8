package com.readhub.bookmanagement.service;

import com.readhub.bookmanagement.model.Transaction;
import com.readhub.bookmanagement.repository.TransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class OverdueScheduler {

    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    public OverdueScheduler(TransactionRepository transactionRepository, EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1:00 AM
    public void checkOverdueTransactions() {
        List<Transaction> overdueTransactions = transactionRepository.findOverdueTransactions();
        
        // Hand off the execution to the background thread pool instantly
        for (Transaction tx : overdueTransactions) {
            emailService.sendOverdueNotificationAsync(
                tx.getUser().getEmail(), 
                tx.getBook().getTitle()
            );
        }
    }
}