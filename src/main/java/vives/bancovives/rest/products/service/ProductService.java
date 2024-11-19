package vives.bancovives.rest.products.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.model.Product;

import java.util.Optional;

public interface ProductService {
    Page<Product> findAll(
        Optional<String> productType,
        Optional<Boolean> isDeleted,
        Optional<String> name,
        Pageable pageable
    );
    Product findById(Long id);
    Product save(InputProduct product);
    Product deleteById(Long id, Boolean logical);
    Product updateById(Long id, InputProduct updatedProduct);

}
