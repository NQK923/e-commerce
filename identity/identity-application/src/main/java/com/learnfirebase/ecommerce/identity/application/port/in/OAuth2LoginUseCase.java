package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.common.application.UseCase;
import com.learnfirebase.ecommerce.identity.application.command.OAuth2LoginCommand;
import com.learnfirebase.ecommerce.identity.application.dto.AuthTokenDto;

public interface OAuth2LoginUseCase extends UseCase {
    AuthTokenDto execute(OAuth2LoginCommand command);
}
