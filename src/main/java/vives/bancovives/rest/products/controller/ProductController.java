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

@Slf4j
@RestController
@RequestMapping("${api.version}/products")
public class ProductController {

    private final ProductService productService;
    private final PaginationLinksUtils paginationLinksUtils;

    @Autowired
    public ProductController(
            ProductService productService,
            PaginationLinksUtils paginationLinksUtils
    ) {
        this.productService = productService;
        this.paginationLinksUtils = paginationLinksUtils;
    }

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

    @PostMapping
    public ResponseEntity<OutputProduct> createProduct(@RequestBody @Valid InputProduct inputProduct) {
        log.info("Creando un nuevo producto");
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.save(inputProduct)
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OutputProduct> getProductById(@PathVariable Long id) {
        log.info("Buscando un producto por ID {}", id);
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.findById(id)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<OutputProduct> updateProduct(@PathVariable Long id, @RequestBody @Valid InputProduct inputProduct) {
        log.info("Actualizando un producto por ID {}", id);
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.updateById(id, inputProduct)
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OutputProduct> deleteProduct(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") Boolean logical
    ) {
        log.info("Borrando un producto por ID {}", id);
        return ResponseEntity.ok(
                ProductMapper.toOutputProduct(
                        productService.deleteById(id, logical)
                )
        );
    }
}
