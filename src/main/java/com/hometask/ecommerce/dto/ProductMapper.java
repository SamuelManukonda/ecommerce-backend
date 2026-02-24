package com.hometask.ecommerce.dto;

import com.hometask.ecommerce.model.Product;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between Product and ProductDto
 */
public final class ProductMapper {
    private ProductMapper() {
    }

    public static ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getCategory(),
                product.getStock(),
                product.getImageUrl(),
                product.getRating()
        );
    }

    /**
     * Converts a list of Product entities to a list of ProductDto objects.
     *
     * @param products the list of Product entities to convert
     * @return a list of ProductDto objects, or an empty list if the input is null or empty
     */
    public static List<ProductDto> toDtoList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }
}

