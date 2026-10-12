
package com.sancruz.inventory;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Display inventory dashboard
    @GetMapping("/")
    public String inventoryDashboard(Model model) {

        List<Product> products = productService.getAllProducts();

        long lowStockCount = products.stream()
                .filter(Product::isLowStock)
                .count();

        model.addAttribute("products", products);
        model.addAttribute("productCount", products.size());
        model.addAttribute("lowStockCount", lowStockCount);

        return "inventory";
    }

    // Display add product form
    @GetMapping("/products/new")
    public String showCreateForm(Model model) {

        model.addAttribute("product", new Product());
        model.addAttribute("pageTitle", "Add Product");
        model.addAttribute("formAction", "/products");

        return "product-form";
    }

    // Save a new product
    @PostMapping("/products")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Product");
            model.addAttribute("formAction", "/products");
            return "product-form";
        }

        try {
            productService.createProduct(product);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product added successfully."
            );

            return "redirect:/";

        } catch (IllegalArgumentException exception) {

            bindingResult.rejectValue(
                    "sku",
                    "duplicate",
                    exception.getMessage()
            );

            model.addAttribute("pageTitle", "Add Product");
            model.addAttribute("formAction", "/products");

            return "product-form";
        }
    }

    // Display edit product form
    @GetMapping("/products/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Product product = productService.getProductById(id);

        model.addAttribute("product", product);
        model.addAttribute("pageTitle", "Edit Product");
        model.addAttribute(
                "formAction",
                "/products/" + id
        );

        return "product-form";
    }

    // Update an existing product
    @PostMapping("/products/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute(
                    "formAction",
                    "/products/" + id
            );

            return "product-form";
        }

        try {
            productService.updateProduct(id, product);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product updated successfully."
            );

            return "redirect:/";

        } catch (IllegalArgumentException exception) {

            bindingResult.rejectValue(
                    "sku",
                    "duplicate",
                    exception.getMessage()
            );

            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute(
                    "formAction",
                    "/products/" + id
            );

            return "product-form";
        }
    }

    // Delete a product
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product deleted successfully."
        );

        return "redirect:/";
    }

    // Increase or decrease product stock
    @PostMapping("/products/{id}/stock")
    public String adjustStock(
            @PathVariable Long id,
            @RequestParam int adjustment,
            RedirectAttributes redirectAttributes) {

        try {
            productService.adjustStock(id, adjustment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Inventory quantity updated."
            );

        } catch (IllegalArgumentException | ArithmeticException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to adjust stock: " + exception.getMessage()
            );
        }

        return "redirect:/";
    }
}
