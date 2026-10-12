
package com.sancruz.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Dell Latitude 7450");
        product.setSku("DELL-7450");
        product.setPrice(new BigDecimal("899.99"));
        product.setQuantity(12);
        product.setMinimumStock(5);
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(product))
                .thenReturn(product);

        Product saved = productService.createProduct(product);

        assertEquals("Dell Latitude 7450", saved.getName());
        verify(productRepository).save(product);
    }

    @Test
    void shouldRejectDuplicateSku() {
        when(productRepository.existsBySkuIgnoreCase("DELL-7450"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> productService.createProduct(product)
                );

        assertEquals("SKU already exists", exception.getMessage());

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldAdjustStockQuantity() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product updated = productService.adjustStock(1L, -8);

        assertEquals(4, updated.getQuantity());
        assertTrue(updated.isLowStock());
    }

    @Test
    void shouldPreventNegativeStock() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.adjustStock(1L, -13)
        );

        assertEquals(12, product.getQuantity());

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldUpdateProduct() {
        Product changes = new Product();
        changes.setName("Updated Dell Laptop");
        changes.setSku("DELL-7450");
        changes.setPrice(new BigDecimal("849.99"));
        changes.setQuantity(8);
        changes.setMinimumStock(5);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product))
                .thenReturn(product);

        Product updated =
                productService.updateProduct(1L, changes);

        assertEquals("Updated Dell Laptop", updated.getName());
        assertEquals(new BigDecimal("849.99"), updated.getPrice());
        assertEquals(8, updated.getQuantity());
    }

    @Test
    void shouldDeleteProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldDetectLowStock() {
        product.setQuantity(4);
        product.setMinimumStock(5);

        assertTrue(product.isLowStock());

        product.setQuantity(8);

        assertFalse(product.isLowStock());
    }
}
