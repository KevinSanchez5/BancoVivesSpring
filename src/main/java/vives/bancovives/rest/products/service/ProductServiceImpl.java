package vives.bancovives.rest.products.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.exceptions.ProductAlreadyExistsException;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;
import vives.bancovives.rest.products.mappers.ProductMapper;
import vives.bancovives.rest.products.model.Product;
import vives.bancovives.rest.products.repositories.ProductRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementa la interfaz ProductService, proporcionando métodos para administrar productos.
 *
 * @author Diego Novillo Luceño
 * @since 1.0.0
 */
@Service
@Slf4j
@CacheConfig(cacheNames = {"products"})
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    /**
     * Constructor para ProductServiceImpl.
     *
     * @param repository el ProductRepository que se utilizará para operaciones de base de datos
     */
    @Autowired
    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Recupera una lista paginada de productos basada en los criterios de búsqueda proporcionados.
     *
     * @param productType parámetro opcional para filtrar productos por tipo
     * @param isDeleted   parámetro opcional para filtrar productos por estado de eliminación
     * @param name        parámetro opcional para filtrar productos por nombre
     * @param pageable    información de paginación
     * @return una página de objetos Product que coinciden con los criterios de búsqueda
     */
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

    /**
     * Recupera un producto por su identificador único.
     *
     * @param id el identificador único del producto
     * @return el objeto Product con el id especificado, o lanza una excepción ProductDoesNotExistException si no se encuentra
     */
    @Override
    @Cacheable(key = "#id")
    public Product findById(UUID id) {
        log.info("Buscando el producto con id: " + id);
        return repository.findById(id).orElseThrow(
                () -> new ProductDoesNotExistException("El producto con id: " + id + " no existe"));
    }

    /**
     * Recupera un producto por su nombre.
     *
     * @param name el nombre del producto
     * @return el objeto Product con el nombre especificado, o lanza una excepción ProductDoesNotExistException si no se encuentra
     */
    @Override
    public Product findByName(String name) {
        log.info("Buscando el producto con nombre: " + name);
        return repository.findByName(name.trim().toUpperCase()).orElseThrow(
                () -> new ProductDoesNotExistException("El producto con nombre: " + name + " no existe"));
    }

    /**
     * Valida que el nombre del producto sea único en el repositorio.
     * Si un producto con el mismo nombre ya existe y su ID es diferente del ID proporcionado,
     * se lanza una {@link ProductAlreadyExistsException}.
     *
     * @param name el nombre del producto a validar
     * @param id   el ID del producto que se está actualizando, o null si se está creando un nuevo producto
     * @throws ProductAlreadyExistsException si un producto con el mismo nombre ya existe y su ID es diferente
     */

    private void validateUniqueProductName(String name, UUID id) {
        Optional<Product> existingProduct = repository.findByName(name.trim().toUpperCase());
        if (existingProduct.isPresent() && (id == null || !existingProduct.get().getId().equals(id))) {
            throw new ProductAlreadyExistsException("El producto con nombre: " + name + " ya existe");
        }
    }

    /**
     * Guarda un nuevo producto en la base de datos después de validar que el nombre del producto sea único.
     *
     * @param product el objeto InputProduct que se va a guardar
     * @return el objeto Product guardado
     * @throws ProductAlreadyExistsException si un producto con el mismo nombre ya existe
     */
    @Override
    @CachePut(key = "#result.id")
    public Product save(InputProduct product) {
        log.info("Guardando el producto: " + product);
        Product mappedProduct = ProductMapper.toProduct(product);
        validateUniqueProductName(mappedProduct.getName(), null);
        return repository.save(mappedProduct);
    }

    /**
     * Elimina un producto de la base de datos.
     *
     * @param id el identificador único del producto
     * @return el objeto Product eliminado, o lanza una excepción ProductDoesNotExistException si no se encuentra
     */
    @Override
    @CacheEvict(key = "#id")
    public Product deleteById(UUID id) {
        log.info("Eliminando el producto con id: " + id);
        Product productToDelete = findById(id);
        productToDelete.setIsDeleted(true);
        productToDelete.setUpdatedAt(LocalDateTime.now());
        return repository.save(productToDelete);
    }

    /**
     * Actualiza un producto existente en la base de datos después de validar que el nombre del producto sea único.
     *
     * @param id             el identificador único del producto
     * @param updatedProduct el objeto InputProduct actualizado
     * @return el objeto Product actualizado
     * @throws ProductDoesNotExistException  si el producto con el id especificado no se encuentra
     * @throws ProductAlreadyExistsException si un producto con el mismo nombre ya existe y su ID es diferente
     */
    @Override
    @CachePut(key = "#id")
    public Product updateById(UUID id, InputProduct updatedProduct) {
        log.info("Actualizando el producto con id: " + id);
        Product existingProduct = findById(id);
        validateUniqueProductName(updatedProduct.getName(), id);
        ProductMapper.updateProductFromInput(existingProduct, updatedProduct);
        return repository.save(existingProduct);
    }
}
