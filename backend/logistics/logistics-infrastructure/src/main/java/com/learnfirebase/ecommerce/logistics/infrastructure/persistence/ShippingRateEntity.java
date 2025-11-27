package com.learnfirebase.ecommerce.logistics.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipping_rates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateEntity {
    @Id
    private String id;
    private String methodId;
    private String destination;
    private String cost;
    private String currency;
}
