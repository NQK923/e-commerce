package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;

public interface UserQueryUseCase extends UseCase {
    UserDto getByEmail(String email);

    UserDto getById(String id);
}
