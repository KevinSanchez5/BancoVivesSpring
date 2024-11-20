package vives.bancovives.rest.products.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.dto.output.OutputProduct;
import vives.bancovives.rest.products.mappers.ProductMapper;
import vives.bancovives.rest.products.service.ProductService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.nio.file.LinkOption;
import java.util.Optional;
import java.util.UUID;

/**
 * Esta clase se encarga de manejar las solicitudes HTTP relacionadas con los productos.
 * Proporciona puntos finales para recuperar, crear, actualizar y eliminar productos.
 *
 * @author Diego Novillo Luceño
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("${api.version}/products")
public class ProductController {

    private final ProductService productService;
    private final PaginationLinksUtils paginationLinksUtils;

    /**
     * Constructor para ProductController.
     *
     * @param productService       El servicio para administrar productos.
     * @param paginationLinksUtils La utilidad para crear enlaces de paginación.
     */
    @Autowired
    public ProductController(
            ProductService productService,
            PaginationLinksUtils paginationLinksUtils
    ) {
        this.productService = productService;
        this.paginationLinksUtils = paginationLinksUtils;
    }

    /**
     * Recupera una lista de productos basada en los filtros proporcionados y parámetros de paginación.
     *
     * @param productType El tipo de producto para filtrar por.
     * @param name        El nombre del producto para filtrar por.
     * @param isDeleted   Si se filtra por productos eliminados.
     * @param page        El número de página para la paginación.
     * @param size        El número de productos por página.
     * @param sortBy      El campo por el que se ordena.
     * @param direction   La dirección de ordenamiento (asc o desc).
     * @param request     La solicitud HTTP.
     * @return Un ResponseEntity que contiene un PageResponse de objetos OutputProduct.
     */
    @GetMapping
    public ResponseEntity<PageResponse<OutputProduct>> getProducts(
            @RequestParam(required = false) Optional<String> productType,
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(required = false) Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
    ) {
        log.info("Buscando todos los productos");
        // Creamos el objeto de ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Creamos cómo va a ser la paginación
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<OutputProduct> pageResult = productService.findAll(productType, isDeleted, name, PageRequest.of(page, size, sort))
                .map(ProductMapper::toOutputProduct);
        return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Crea un nuevo producto.
     *
     * @param inputProduct el producto que se quiere crear
     */
    @PostMapping
    public ResponseEntity<OutputProduct> createProduct(@RequestBody @Valid InputProduct inputProduct) {
        log.info("Creando un nuevo producto");
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.save(inputProduct)
                )
        );
    }

    /**
     * Recupera un producto por su ID.
     *
     * @param id el ID del producto que se quiere recuperar
     */
    @GetMapping("/{id}")
    public ResponseEntity<OutputProduct> getProductById(@PathVariable UUID id) {
        log.info("Buscando un producto por ID {}", id);
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.findById(id)
                )
        );
    }

    /**
     * Actualiza un producto por su ID.
     *
     * @param id el ID del producto que se quiere actualizar
     * @param inputProduct los cambios que se desean aplicar al producto
     */
    @PutMapping("/{id}")
    public ResponseEntity<OutputProduct> updateProduct(
            @PathVariable UUID id,
            @RequestBody @Valid InputProduct inputProduct
    ) {
        log.info("Actualizando un producto por ID {}", id);
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.updateById(id, inputProduct)
                )
        );
    }

    /**
     * Elimina un producto por su ID.
     *
     * @param id el ID del producto que se quiere eliminar
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OutputProduct> deleteProduct(
            @PathVariable UUID id
    ) {
        log.info("Borrando un producto por ID {}", id);
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.deleteById(id)
                )
        );
    }
}
