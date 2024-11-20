package vives.bancovives.rest.products.mappers;

import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.dto.output.OutputProduct;
import vives.bancovives.rest.products.model.Product;

/**
 * Esta clase proporciona métodos para mapear entre objetos {@link Product} y {@link OutputProduct}, así como entre
 * objetos {@link InputProduct} y {@link Product}.
 */
public class ProductMapper {
    private ProductMapper() {}

    /**
     * Mapea un objeto {@link Product} a un objeto {@link OutputProduct}.
     *
     * @param product El objeto {@link Product} que se va a mapear.
     * @return Un objeto {@link OutputProduct} que contiene los datos mapeados.
     */
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

    /**
     * Mapea un objeto {@link InputProduct} a un objeto {@link Product}.
     *
     * @param inputProduct El objeto {@link InputProduct} que se va a mapear.
     * @return Un objeto {@link Product} que contiene los datos mapeados.
     */
    public static Product toProduct(InputProduct inputProduct) {
        return Product.builder()
                .name(inputProduct.getName().trim().toUpperCase())
                .description(inputProduct.getDescription())
                .productType(inputProduct.getProductType().trim().toUpperCase())
                .interest(inputProduct.getInterest())
                .build();
    }
}
