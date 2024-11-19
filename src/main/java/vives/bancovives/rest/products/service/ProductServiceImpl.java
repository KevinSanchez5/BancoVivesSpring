package vives.bancovives.rest.products.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;
import vives.bancovives.rest.products.mappers.ProductMapper;
import vives.bancovives.rest.products.model.Product;
import vives.bancovives.rest.products.repositories.ProductRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    @Autowired
    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Product> findAll(
            Optional<String> productType,
            Optional<Boolean> isDeleted,
            Optional<String> name,
            Pageable pageable

    ) {
        log.info("Buscando todos los productos");
        // Criterio de búsqueda por nombre
        Specification<Product> nameSpec = (root, query, criteriaBuilder) ->
                name.map(m -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por el tipo de producto
        Specification<Product> productTypeSpec = (root, query, criteriaBuilder) ->
                productType.map(p -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), p))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por si el producto está eliminado o no
        Specification<Product> specIsDeleted = (root, query, criteriaBuilder) ->
                isDeleted.map(d -> criteriaBuilder.equal(root.get("isDeleted"), d))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));


        Specification<Product> criterio = Specification.where(nameSpec)
                .and(productTypeSpec)
                .and(specIsDeleted);
        return repository.findAll(criterio, pageable);
    }

    @Override
    public Product findById(Long id) {
        log.info("Buscando el producto con id: " + id);
        return repository.findById(id).orElseThrow(() -> new ProductDoesNotExistException("El producto con id: " + id + " no existe"));
    }

    @Override
    public Product save(InputProduct product) {
        log.info("Guardando el producto: " + product);
        return repository.save(
                ProductMapper.toProduct(product)
        );
    }

    @Override
    public Product deleteById(Long id, Boolean logical) {
        log.info("Eliminando el producto con id: " + id);
        Optional<Product> result = repository.findById(id);
        if (result.isPresent()) {
            Product productToDelete = result.get();
            productToDelete.setIsDeleted(true);
            if (logical) {
                repository.save(productToDelete);
            }else repository.deleteById(id);
            return productToDelete;
        } else {
            throw new ProductDoesNotExistException("El producto con id: " + id + " no existe");
        }
    }

    @Override
    public Product updateById(Long id, InputProduct updatedProduct) {
        log.info("Actualizando el producto con id: " + id);
        Optional<Product> result = repository.findById(id);
        if (result.isPresent()) {
            Product existingProduct = result.get();
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setUpdatedAt(LocalDateTime.now());
            return repository.save(existingProduct);
        } else {
            throw new ProductDoesNotExistException("El producto con id: " + id + " no existe");
        }
    }
}
