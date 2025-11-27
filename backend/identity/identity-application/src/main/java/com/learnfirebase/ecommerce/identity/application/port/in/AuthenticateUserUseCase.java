package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.identity.application.command.LoginCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;

public interface AuthenticateUserUseCase extends UseCase {
    AuthTokenDto execute(LoginCommand command);
}
