package com.learnfirebase.ecommerce.identity.application.command;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateProfileCommand {
    String userId;
    String email;
    String displayName;
    String avatarUrl;
    String shopDescription;
    String shopBannerUrl;
}
