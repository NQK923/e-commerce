package com.learnfirebase.ecommerce.order.application.port.out;

import java.util.Optional;

import com.learnfirebase.ecommerce.order.application.model.PaymentRecord;
import com.learnfirebase.ecommerce.order.application.model.PaymentStatus;

public interface PaymentTransactionPort {
    void save(PaymentRecord record);
    Optional<PaymentRecord> findByReference(String reference);
    void updateStatus(String reference, PaymentStatus status, String transactionNo, String rawPayload);
}
