package com.learnfirebase.ecommerce.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.learnfirebase.ecommerce.common.domain.DomainEvent;
import com.learnfirebase.ecommerce.common.domain.valueobject.Money;
import com.learnfirebase.ecommerce.product.application.command.UpsertProductCommand;
import com.learnfirebase.ecommerce.product.application.command.DeleteProductCommand;
import com.learnfirebase.ecommerce.product.application.dto.ProductDto;
import com.learnfirebase.ecommerce.product.application.port.out.ProductEventPublisher;
import com.learnfirebase.ecommerce.product.application.port.out.ProductRepository;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchIndexPort;
import com.learnfirebase.ecommerce.product.application.port.out.ProductSearchPort;
import com.learnfirebase.ecommerce.product.domain.event.ProductCreatedEvent;
import com.learnfirebase.ecommerce.product.domain.exception.ProductDomainException;
import com.learnfirebase.ecommerce.common.domain.AccessDeniedDomainException;
import com.learnfirebase.ecommerce.common.domain.ResourceNotFoundDomainException;
import com.learnfirebase.ecommerce.product.domain.model.Category;
import com.learnfirebase.ecommerce.product.domain.model.Product;
import com.learnfirebase.ecommerce.product.domain.model.ProductId;
import com.learnfirebase.ecommerce.product.domain.model.ProductImage;
import com.learnfirebase.ecommerce.product.domain.model.ProductImageId;
import com.learnfirebase.ecommerce.product.domain.model.ProductVariant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductSearchIndexPort productSearchIndexPort;
    @Mock
    private ProductSearchPort productSearchPort;
    @Mock
    private ProductEventPublisher productEventPublisher;

    private ProductApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ProductApplicationService(
            productRepository,
            productSearchIndexPort,
            productSearchPort,
            productEventPublisher
        );
    }

    @Test
    void createProductAssignsSellerPersistsIndexesAndPublishesInitialStockEvent() {
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDto result = service.execute(createCommand(null, "seller-1"));

        assertThat(result.getSellerId()).isEqualTo("seller-1");
        assertThat(result.getName()).isEqualTo("Smoke Keyboard");
        assertThat(result.getPrice()).isEqualTo("1200000");
        assertThat(result.getVariants()).hasSize(1);
        ArgumentCaptor<Product> savedProduct = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(savedProduct.capture());
        assertThat(savedProduct.getValue().getSellerId()).isEqualTo("seller-1");
        assertThat(savedProduct.getValue().getVariants()).hasSize(1);
        verify(productSearchIndexPort).index(savedProduct.getValue());

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(productEventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(ProductCreatedEvent.class);
        ProductCreatedEvent event = (ProductCreatedEvent) eventCaptor.getValue();
        assertThat(event.getProductId()).isEqualTo(savedProduct.getValue().getId().getValue());
        assertThat(event.getInitialStock()).isEqualTo(12);
        assertThat(event.getVariants()).singleElement()
            .satisfies(variant -> {
                assertThat(variant.getSku()).isEqualTo("SKU-KEYBOARD-BLACK");
                assertThat(variant.getQuantity()).isEqualTo(5);
            });
    }

    @Test
    void updateProductRejectsSellerThatDoesNotOwnExistingProduct() {
        Product existing = product("product-1", "seller-1");
        when(productRepository.findById(new ProductId("product-1"))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(createCommand("product-1", "seller-2")))
            .isInstanceOf(AccessDeniedDomainException.class)
            .hasMessage("You are not authorized to update this product");

        verify(productRepository, never()).save(any());
        verify(productSearchIndexPort, never()).index(any());
        verify(productEventPublisher, never()).publish(any());
    }

    @Test
    void updateProductByOwnerKeepsExistingVariantsAndImagesWhenPayloadOmitsThem() {
        Product existing = product("product-1", "seller-1");
        when(productRepository.findById(new ProductId("product-1"))).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDto result = service.execute(UpsertProductCommand.builder()
            .id("product-1")
            .name("Updated Keyboard")
            .description("Updated description")
            .price("1300000")
            .currency("VND")
            .categoryId("electronics")
            .sellerId("seller-1")
            .quantity(9)
            .build());

        assertThat(result.getName()).isEqualTo("Updated Keyboard");
        assertThat(result.getSellerId()).isEqualTo("seller-1");
        assertThat(result.getVariants()).singleElement()
            .satisfies(variant -> assertThat(variant.getSku()).isEqualTo("SKU-EXISTING"));
        assertThat(result.getImages()).singleElement()
            .satisfies(image -> assertThat(image.getUrl()).isEqualTo("https://example.local/existing.png"));
        verify(productSearchIndexPort).index(any(Product.class));
        verify(productEventPublisher, never()).publish(any());
    }

    @Test
    void searchIndexFailureDoesNotFailSellerMutation() {
        when(productRepository.findById(any(ProductId.class))).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("search unavailable")).when(productSearchIndexPort).index(any(Product.class));

        ProductDto result = service.execute(createCommand(null, "seller-1"));

        assertThat(result.getSellerId()).isEqualTo("seller-1");
        verify(productEventPublisher).publish(any(ProductCreatedEvent.class));
    }

    @Test
    void createProductRequiresSellerId() {
        assertThatThrownBy(() -> service.execute(createCommand(null, " ")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Product must have a seller");

        verify(productRepository, never()).save(any());
        verify(productEventPublisher, never()).publish(any());
    }

    @Test
    void deleteProductSuccessfullyAsOwner() {
        Product existing = product("product-1", "seller-1");
        when(productRepository.findById(new ProductId("product-1"))).thenReturn(Optional.of(existing));

        service.delete(DeleteProductCommand.builder().id("product-1").sellerId("seller-1").isAdmin(false).build());

        verify(productRepository).delete(new ProductId("product-1"));
        verify(productSearchIndexPort).deleteIndex("product-1");
    }

    @Test
    void deleteProductSuccessfullyAsAdmin() {
        Product existing = product("product-1", "seller-1");
        when(productRepository.findById(new ProductId("product-1"))).thenReturn(Optional.of(existing));

        service.delete(DeleteProductCommand.builder().id("product-1").sellerId("seller-2").isAdmin(true).build());

        verify(productRepository).delete(new ProductId("product-1"));
        verify(productSearchIndexPort).deleteIndex("product-1");
    }

    @Test
    void deleteProductThrowsAccessDeniedForNonOwner() {
        Product existing = product("product-1", "seller-1");
        when(productRepository.findById(new ProductId("product-1"))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.delete(DeleteProductCommand.builder().id("product-1").sellerId("seller-2").isAdmin(false).build()))
            .isInstanceOf(AccessDeniedDomainException.class)
            .hasMessage("You are not authorized to delete this product");

        verify(productRepository, never()).delete(any());
        verify(productSearchIndexPort, never()).deleteIndex(any());
    }

    @Test
    void deleteProductThrowsResourceNotFoundForMissingProduct() {
        when(productRepository.findById(new ProductId("product-1"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(DeleteProductCommand.builder().id("product-1").sellerId("seller-1").isAdmin(false).build()))
            .isInstanceOf(ResourceNotFoundDomainException.class)
            .hasMessage("Product not found: product-1");

        verify(productRepository, never()).delete(any());
        verify(productSearchIndexPort, never()).deleteIndex(any());
    }

    @Test
    void deleteProductToleratesSearchIndexUnindexingFailure() {
        Product existing = product("product-1", "seller-1");
        when(productRepository.findById(new ProductId("product-1"))).thenReturn(Optional.of(existing));
        doThrow(new RuntimeException("search index down")).when(productSearchIndexPort).deleteIndex("product-1");

        service.delete(DeleteProductCommand.builder().id("product-1").sellerId("seller-1").isAdmin(false).build());

        verify(productRepository).delete(new ProductId("product-1"));
        verify(productSearchIndexPort).deleteIndex("product-1");
    }

    private UpsertProductCommand createCommand(String productId, String sellerId) {
        return UpsertProductCommand.builder()
            .id(productId)
            .name("Smoke Keyboard")
            .description("Seller product create smoke")
            .price("1200000")
            .currency("VND")
            .categoryId("electronics")
            .sellerId(sellerId)
            .quantity(12)
            .variant(UpsertProductCommand.VariantCommand.builder()
                .sku("SKU-KEYBOARD-BLACK")
                .name("Black")
                .price("1250000")
                .quantity(5)
                .build())
            .image(UpsertProductCommand.ImageCommand.builder()
                .id("img-1")
                .url("https://example.local/keyboard.png")
                .sortOrder(0)
                .primaryImage(true)
                .build())
            .build();
    }

    private Product product(String productId, String sellerId) {
        return Product.builder()
            .id(new ProductId(productId))
            .name("Existing Keyboard")
            .description("Existing product")
            .price(Money.builder().amount(new BigDecimal("1100000")).currency("VND").build())
            .stock(10)
            .sellerId(sellerId)
            .category(Category.builder().id("electronics").name("electronics").build())
            .variants(List.of(ProductVariant.builder()
                .sku("SKU-EXISTING")
                .name("Existing")
                .price(Money.builder().amount(new BigDecimal("1100000")).currency("VND").build())
                .quantity(10)
                .build()))
            .images(List.of(ProductImage.builder()
                .id(new ProductImageId("img-existing"))
                .url("https://example.local/existing.png")
                .sortOrder(0)
                .primary(true)
                .build()))
            .createdAt(Instant.parse("2026-06-15T00:00:00Z"))
            .updatedAt(Instant.parse("2026-06-15T00:00:00Z"))
            .build();
    }
}
