
package com.sancruz.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Retrieve all products
    public List<Product> getAllProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    // Find a specific product
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found"));
    }

    // Add a new product
    @Transactional
    public Product createProduct(Product product) {

        if (productRepository.existsBySkuIgnoreCase(product.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        return productRepository.save(product);
    }

    // Update an existing product
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {

        Product existing = getProductById(id);

        if (!existing.getSku().equalsIgnoreCase(updatedProduct.getSku())
                && productRepository.existsBySkuIgnoreCase(updatedProduct.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        existing.setName(updatedProduct.getName());
        existing.setSku(updatedProduct.getSku());
        existing.setPrice(updatedProduct.getPrice());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setMinimumStock(updatedProduct.getMinimumStock());

        return productRepository.save(existing);
    }

    // Delete a product
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    // Adjust inventory quantity
    @Transactional
    public Product adjustStock(Long id, int adjustment) {

        Product product = getProductById(id);

        int newQuantity = Math.addExact(product.getQuantity(), adjustment);

        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    "Inventory quantity cannot be negative"
            );
        }

        product.setQuantity(newQuantity);

        return productRepository.save(product);
    }
}
