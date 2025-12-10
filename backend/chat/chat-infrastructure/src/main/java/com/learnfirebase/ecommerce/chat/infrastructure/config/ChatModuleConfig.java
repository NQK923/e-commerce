package com.learnfirebase.ecommerce.chat.infrastructure.config;

import com.learnfirebase.ecommerce.chat.application.port.out.MessageDeliveryPort;
import com.learnfirebase.ecommerce.chat.application.port.out.NotificationEventPort;
import com.learnfirebase.ecommerce.chat.application.port.out.PresencePort;
import com.learnfirebase.ecommerce.chat.application.port.in.GetConversationsUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.GetMessagesUseCase;
import com.learnfirebase.ecommerce.chat.application.port.in.MarkConversationReadUseCase;
import com.learnfirebase.ecommerce.chat.application.service.ChatQueryService;
import com.learnfirebase.ecommerce.chat.application.service.SendMessageService;
import com.learnfirebase.ecommerce.chat.application.usecase.SendMessageUseCase;
import com.learnfirebase.ecommerce.chat.domain.repository.ConversationRepository;
import com.learnfirebase.ecommerce.chat.domain.repository.MessageRepository;
import com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa.ConversationJpaEntity;
import com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa.ConversationJpaRepository;
import com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa.MessageJpaEntity;
import com.learnfirebase.ecommerce.chat.infrastructure.persistence.jpa.MessageJpaRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = {ConversationJpaRepository.class, MessageJpaRepository.class})
@EntityScan(basePackageClasses = {ConversationJpaEntity.class, MessageJpaEntity.class})
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
                                             MessageRepository messageRepository) {
        return new ChatQueryService(conversationRepository, messageRepository);
    }

    @Bean
    public GetConversationsUseCase getConversationsUseCase(ChatQueryService chatQueryService) {
        return chatQueryService;
    }

    @Bean
    public GetMessagesUseCase getMessagesUseCase(ChatQueryService chatQueryService) {
        return chatQueryService;
    }

    @Bean
    public MarkConversationReadUseCase markConversationReadUseCase(ChatQueryService chatQueryService) {
        return chatQueryService;
    }
}
