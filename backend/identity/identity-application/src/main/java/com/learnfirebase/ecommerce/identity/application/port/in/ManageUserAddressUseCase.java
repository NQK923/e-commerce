package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.identity.application.command.AddAddressCommand;
import com.learnfirebase.ecommerce.identity.application.dto.UserAddressDto;

public interface ManageUserAddressUseCase {
    UserAddressDto addAddress(String userId, AddAddressCommand command);
    void deleteAddress(String userId, String addressId);
}
