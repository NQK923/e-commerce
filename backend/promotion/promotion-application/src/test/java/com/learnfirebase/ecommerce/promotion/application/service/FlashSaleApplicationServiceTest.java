package com.learnfirebase.ecommerce.promotion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.promotion.application.command.CreateFlashSaleCommand;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleCachePort;
import com.learnfirebase.ecommerce.promotion.application.port.out.FlashSaleRepository;
import com.learnfirebase.ecommerce.promotion.domain.exception.PromotionDomainException;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSale;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleId;
import com.learnfirebase.ecommerce.promotion.domain.model.FlashSaleStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlashSaleApplicationServiceTest {
    @Mock
    private FlashSaleRepository flashSaleRepository;
    @Mock
    private FlashSaleCachePort flashSaleCachePort;

    private FlashSaleApplicationService service;

    @BeforeEach
    void setUp() {
        service = new FlashSaleApplicationService(flashSaleRepository, flashSaleCachePort);
    }

    @Test
    void adminCreateFlashSalePersistsAndWarmsStockCacheWhenActive() {
        when(flashSaleRepository.save(org.mockito.ArgumentMatchers.any(FlashSale.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        FlashSaleId id = service.createFlashSale(validCommand());

        assertThat(id).isNotNull();
        ArgumentCaptor<FlashSale> flashSaleCaptor = ArgumentCaptor.forClass(FlashSale.class);
        verify(flashSaleRepository).save(flashSaleCaptor.capture());
        FlashSale saved = flashSaleCaptor.getValue();
        assertThat(saved.getProductId()).isEqualTo("product-1");
        assertThat(saved.getPrice()).isEqualTo(Money.builder().amount(new BigDecimal("90000")).currency("VND").build());
        assertThat(saved.getOriginalPrice()).isEqualTo(Money.builder().amount(new BigDecimal("120000")).currency("VND").build());
        assertThat(saved.getRemainingQuantity()).isEqualTo(30);
        assertThat(saved.getStatus()).isEqualTo(FlashSaleStatus.ACTIVE);
        verify(flashSaleCachePort).setStock(saved.getId(), 30);
    }

    @Test
    void adminCreateFlashSaleRejectsInvalidDiscountAndDoesNotPersist() {
        CreateFlashSaleCommand command = CreateFlashSaleCommand.builder()
            .productId("product-1")
            .price(new BigDecimal("120000"))
            .currency("VND")
            .originalPrice(new BigDecimal("120000"))
            .originalCurrency("VND")
            .startTime(Instant.now().minusSeconds(60))
            .endTime(Instant.now().plusSeconds(3600))
            .totalQuantity(30)
            .build();

        assertThatThrownBy(() -> service.createFlashSale(command))
            .isInstanceOf(PromotionDomainException.class)
            .hasMessage("Flash sale price must be lower than original price");

        verify(flashSaleRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(flashSaleCachePort, never()).setStock(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void adminCreateFlashSaleRejectsInvalidWindowAndQuantity() {
        CreateFlashSaleCommand invalidWindow = validCommand().toBuilder()
            .startTime(Instant.parse("2026-06-15T11:00:00Z"))
            .endTime(Instant.parse("2026-06-15T10:00:00Z"))
            .build();

        assertThatThrownBy(() -> service.createFlashSale(invalidWindow))
            .isInstanceOf(PromotionDomainException.class)
            .hasMessage("Flash sale end time must be after start time");

        CreateFlashSaleCommand invalidQuantity = validCommand().toBuilder()
            .totalQuantity(0)
            .build();

        assertThatThrownBy(() -> service.createFlashSale(invalidQuantity))
            .isInstanceOf(PromotionDomainException.class)
            .hasMessage("Flash sale quantity must be greater than zero");
    }

    @Test
    void listMethodsDelegateToRepository() {
        FlashSale active = flashSale("active");
        FlashSale ended = flashSale("ended");
        when(flashSaleRepository.findAllActive()).thenReturn(List.of(active));
        when(flashSaleRepository.findAll()).thenReturn(List.of(active, ended));

        assertThat(service.listActiveFlashSales()).containsExactly(active);
        assertThat(service.listAllFlashSales()).containsExactly(active, ended);
    }

    private CreateFlashSaleCommand validCommand() {
        return CreateFlashSaleCommand.builder()
            .productId("product-1")
            .price(new BigDecimal("90000"))
            .currency("VND")
            .originalPrice(new BigDecimal("120000"))
            .originalCurrency("VND")
            .startTime(Instant.now().minusSeconds(60))
            .endTime(Instant.now().plusSeconds(3600))
            .totalQuantity(30)
            .build();
    }

    private FlashSale flashSale(String id) {
        return FlashSale.builder()
            .id(new FlashSaleId(java.util.UUID.nameUUIDFromBytes(id.getBytes(java.nio.charset.StandardCharsets.UTF_8))))
            .productId("product-1")
            .price(Money.builder().amount(new BigDecimal("90000")).currency("VND").build())
            .originalPrice(Money.builder().amount(new BigDecimal("120000")).currency("VND").build())
            .startTime(Instant.now().minusSeconds(60))
            .endTime(Instant.now().plusSeconds(3600))
            .totalQuantity(30)
            .remainingQuantity(30)
            .status(FlashSaleStatus.ACTIVE)
            .build();
    }
}
