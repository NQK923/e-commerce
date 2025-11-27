package com.learnfirebase.ecommerce.logistics.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.logistics.application.command.CreateShipmentCommand;
import com.learnfirebase.ecommerce.logistics.application.dto.ShipmentDto;

public interface CreateShipmentUseCase extends UseCase {
    ShipmentDto execute(CreateShipmentCommand command);
}
