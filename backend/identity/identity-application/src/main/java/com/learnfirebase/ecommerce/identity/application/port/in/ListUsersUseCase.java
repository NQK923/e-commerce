package com.learnfirebase.ecommerce.identity.application.port.in;

import java.util.List;

import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.common.application.UseCase;

public interface ListUsersUseCase extends UseCase {
    List<UserDto> list();
}
