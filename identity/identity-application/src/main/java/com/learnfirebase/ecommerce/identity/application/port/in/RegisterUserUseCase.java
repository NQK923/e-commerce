package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.identity.application.command.RegisterUserCommand;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;

public interface RegisterUserUseCase extends UseCase<RegisterUserCommand, UserDto> {
}
