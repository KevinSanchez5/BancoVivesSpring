package vives.bancovives.rest.products.mappers;

import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.dto.output.OutputProduct;
import vives.bancovives.rest.products.model.Product;

import java.time.LocalDateTime;

/**
 * Esta clase proporciona métodos para mapear entre objetos {@link Product} y {@link OutputProduct}, así como entre
 * objetos {@link InputProduct} y {@link Product}.
 */
public class ProductMapper {

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

    /**
     * Actualiza los campos de un objeto {@link Product} existente con los valores de un objeto {@link InputProduct}.
     *
     * @param existingProduct el objeto {@link Product} que se va a actualizar
     * @param updatedProduct  el objeto {@link InputProduct} que contiene los nuevos valores
     */
    public static void updateProductFromInput(Product existingProduct, InputProduct updatedProduct) {
        existingProduct.setName(updatedProduct.getName().trim().toUpperCase());
        existingProduct.setProductType(updatedProduct.getProductType().trim().toUpperCase());
        existingProduct.setInterest(updatedProduct.getInterest());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setUpdatedAt(LocalDateTime.now());
    }
}
