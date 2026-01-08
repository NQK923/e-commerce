package com.learnfirebase.ecommerce.chat.infrastructure.config;

import com.learnfirebase.ecommerce.chat.application.port.out.MessageDeliveryPort;
import com.learnfirebase.ecommerce.chat.application.port.out.NotificationEventPort;
import com.learnfirebase.ecommerce.chat.application.port.out.PresencePort;
import com.learnfirebase.ecommerce.chat.application.service.ChatQueryService;
import com.learnfirebase.ecommerce.chat.application.service.SendMessageService;
import com.learnfirebase.ecommerce.chat.application.usecase.SendMessageUseCase;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;
import com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo.ConversationMongoRepository;
import com.learnfirebase.ecommerce.chat.infrastructure.persistence.mongo.MessageMongoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackageClasses = {ConversationMongoRepository.class, MessageMongoRepository.class})
public class ChatModuleConfig {

    @Bean
    public SendMessageUseCase sendMessageUseCase(ConversationRepository conversationRepository,
                                                 MessageRepository messageRepository,
                                                 MessageDeliveryPort messageDeliveryPort,
                                                 PresencePort presencePort,
                                                 NotificationEventPort notificationEventPort) {
        return new SendMessageService(
                conversationRepository,
                messageRepository,
                messageDeliveryPort,
                presencePort,
                notificationEventPort
        );
    }

    @Bean
    public ChatQueryService chatQueryService(ConversationRepository conversationRepository,
                                             MessageRepository messageRepository,
                                             MessageDeliveryPort messageDeliveryPort) {
        return new ChatQueryService(conversationRepository, messageRepository, messageDeliveryPort);
    }
}
