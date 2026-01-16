package com.learnfirebase.ecommerce.identity.infrastructure.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.learnfirebase.ecommerce.common.domain.valueobject.Email;
import com.learnfirebase.ecommerce.identity.application.port.out.SellerApplicationRepository;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplication;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationId;
import com.learnfirebase.ecommerce.identity.domain.model.SellerApplicationStatus;
import com.learnfirebase.ecommerce.identity.domain.model.UserId;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SellerApplicationRepositoryImpl implements SellerApplicationRepository {
    private final SellerApplicationJpaRepository jpaRepository;

    @Override
    public SellerApplication save(SellerApplication application) {
        SellerApplicationEntity entity = toEntity(application);
        SellerApplicationEntity saved = jpaRepository.save(Objects.requireNonNull(entity));
        return toDomain(saved);
    }

    @Override
    public Optional<SellerApplication> findById(SellerApplicationId id) {
        return jpaRepository.findById(Objects.requireNonNull(id.getValue())).map(this::toDomain);
    }

    @Override
    public Optional<SellerApplication> findLatestByUserId(UserId userId) {
        return jpaRepository.findTopByUserIdOrderByCreatedAtDesc(userId.getValue()).map(this::toDomain);
    }

    @Override
    public List<SellerApplication> findAll() {
        return jpaRepository.findAll().stream()
                .sorted(Comparator.comparing(SellerApplicationEntity::getCreatedAt).reversed())
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<SellerApplication> findByStatus(SellerApplicationStatus status) {
        return jpaRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toDomain)
                .toList();
    }

    private SellerApplicationEntity toEntity(SellerApplication application) {
        return SellerApplicationEntity.builder()
                .id(application.getId().getValue())
                .userId(application.getUserId().getValue())
                .storeName(application.getStoreName())
                .contactEmail(application.getContactEmail() != null ? application.getContactEmail().getValue() : null)
                .phone(application.getPhone())
                .category(application.getCategory())
                .description(application.getDescription())
                .avatarUrl(application.getAvatarUrl())
                .coverUrl(application.getCoverUrl())
                .status(application.getStatus())
                .acceptedTerms(application.isAcceptedTerms())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private SellerApplication toDomain(SellerApplicationEntity entity) {
        return SellerApplication.builder()
                .id(new SellerApplicationId(entity.getId()))
                .userId(new UserId(entity.getUserId()))
                .storeName(entity.getStoreName())
                .contactEmail(entity.getContactEmail() != null ? new Email(entity.getContactEmail()) : null)
                .phone(entity.getPhone())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .avatarUrl(entity.getAvatarUrl())
                .coverUrl(entity.getCoverUrl())
                .status(entity.getStatus())
                .acceptedTerms(entity.isAcceptedTerms())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
