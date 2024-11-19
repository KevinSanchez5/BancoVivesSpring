package vives.bancovives.rest.products.mappers;

import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.dto.output.OutputProduct;
import vives.bancovives.rest.products.model.Product;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductMapper {
    private ProductMapper() {}

    public static OutputProduct toOutputProduct(Product product) {
        return OutputProduct.builder()
                .name(product.getName())
                .id(product.getId())
                .description(product.getDescription())
                .interest(product.getInterest())
                .createdAt(product.getCreatedAt().toString())
                .updatedAt(product.getUpdatedAt().toString())
                .isDeleted(product.getIsDeleted())
                .productType(product.getProductType())
                .build();
    }

    public static Product toProduct(InputProduct inputProduct) {
        return Product.builder()
                .id(UUID.fromString(inputProduct.getId()))
                .name(inputProduct.getName())
                .description(inputProduct.getDescription())
                .productType(inputProduct.getProductType())
                .interest(inputProduct.getInterest())
                .createdAt(LocalDateTime.parse(inputProduct.getCreatedAt()))
                .updatedAt(LocalDateTime.parse(inputProduct.getUpdatedAt()))
                .isDeleted(inputProduct.getIsDeleted())
                .build();
    }
}
