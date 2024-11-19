package vives.bancovives.rest.products.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    Page<Product> findAll(
        Optional<String> productType,
        Optional<Boolean> isDeleted,
        Optional<String> name,
        Pageable pageable
    );
    Product findById(UUID id);
    Product save(InputProduct product);
    Product deleteById(UUID id, Boolean logical);
    Product updateById(UUID id, InputProduct updatedProduct);

}
