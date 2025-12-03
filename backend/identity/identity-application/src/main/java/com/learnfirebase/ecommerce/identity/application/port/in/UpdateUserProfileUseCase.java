package com.learnfirebase.ecommerce.identity.application.port.in;

import com.learnfirebase.ecommerce.identity.application.command.UpdateProfileCommand;
import com.learnfirebase.ecommerce.identity.application.dto.UserDto;
import com.learnfirebase.ecommerce.common.application.UseCase;

public interface UpdateUserProfileUseCase extends UseCase {
    UserDto execute(UpdateProfileCommand command);
}
