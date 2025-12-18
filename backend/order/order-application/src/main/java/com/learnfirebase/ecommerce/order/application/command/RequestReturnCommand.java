package com.learnfirebase.ecommerce.order.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestReturnCommand {
    private String orderId;
    private String userId;
    private String reason;
    private String note;
}
