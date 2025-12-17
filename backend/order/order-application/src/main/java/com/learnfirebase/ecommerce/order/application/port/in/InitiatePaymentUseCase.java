package com.learnfirebase.ecommerce.order.application.port.in;

import com.learnfirebase.ecommerce.order.application.command.InitiatePaymentCommand;
import com.learnfirebase.ecommerce.order.application.dto.PaymentInitResponse;

public interface InitiatePaymentUseCase extends UseCase {
    PaymentInitResponse initiate(InitiatePaymentCommand command);
}
