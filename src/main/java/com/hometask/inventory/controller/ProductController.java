package com.hometask.inventory.controller;

import com.hometask.inventory.dto.PaginatedResponse;
import com.hometask.inventory.dto.ProductDto;
import com.hometask.inventory.model.Product;
import com.hometask.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get paginated list of products", description = "Returns a paginated response of products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of products"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size parameter")
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<Product>> getProducts(
            @Parameter(description = "Page index", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must not be less than or equal to zero");
        }

        return ResponseEntity.ok(productService.getProducts(page, size));
    }

    @Operation(summary = "Get all products", description = "Returns a list of all products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of all products")
    })
    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
