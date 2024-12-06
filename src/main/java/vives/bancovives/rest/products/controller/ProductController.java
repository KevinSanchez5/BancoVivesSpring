package vives.bancovives.rest.products.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.dto.input.UpdatedAccountType;
import vives.bancovives.rest.products.accounttype.dto.output.OutputAccountType;
import vives.bancovives.rest.products.accounttype.mappers.AccountTypeMapper;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.service.AccountTypeService;
import vives.bancovives.rest.products.accounttype.storage.AccountTypeStorageCSV;
import vives.bancovives.rest.products.cardtype.dto.input.NewCardType;
import vives.bancovives.rest.products.cardtype.dto.input.UpdatedCardType;
import vives.bancovives.rest.products.cardtype.dto.output.OutputCardType;
import vives.bancovives.rest.products.cardtype.mappers.CardTypeMapper;
import vives.bancovives.rest.products.cardtype.service.CardTypeService;
import vives.bancovives.rest.products.exceptions.ProductAlreadyExistsException;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;
import vives.bancovives.rest.products.exceptions.ProductStorageException;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
@PreAuthorize("hasAnyRole('ADMIN')")
@Tag(name = "API de productos", description = "API para la gestión de productos")
public class ProductController {

    private final CardTypeService cardTypeService;
    private final AccountTypeService accountTypeService;
    private final PaginationLinksUtils paginationLinksUtils;
    private final AccountTypeStorageCSV accountTypeStorageCSV;

    /**
     * Constructor para ProductController.
     *
     * @param cardTypeService El servicio para administrar los tipos de cartas.
     * @param paginationLinksUtils La utilidad para crear enlaces de paginación
     * @param accountTypeService El servicio para administrar los tipos de cuentas
     * @param accountTypeStorageCSV El servicio para administrar archivos CSV para los tipos de cuentas
     */
    @Autowired
    public ProductController(
            CardTypeService cardTypeService,
            AccountTypeService accountTypeService,
            AccountTypeStorageCSV accountTypeStorageCSV,
            PaginationLinksUtils paginationLinksUtils) {
        this.cardTypeService = cardTypeService;
        this.accountTypeService = accountTypeService;
        this.paginationLinksUtils = paginationLinksUtils;
        this.accountTypeStorageCSV = accountTypeStorageCSV;
    }

    /**
     * Recupera una lista de {@link OutputAccountType} basada en los filtros proporcionados y parámetros de paginación.
     *
     * @param interest    El interés de la cuenta por el que se quiere filtrar la lista.
     * @param name        El nombre del producto por el que se quiere filtrar la lista.
     * @param isDeleted   Sí se filtra por productos eliminados.
     * @param page        El número de página para la paginación.
     * @param size        El número de productos por página.
     * @param sortBy      El campo por el que se ordena.
     * @param direction   La dirección de ordenamiento (asc o desc).
     * @param request     La solicitud HTTP.
     * @return Un {@link ResponseEntity} que contiene un {@link PageResponse} de objetos {@link OutputAccountType}.
     */
    @Operation(description = "Obtiene todos los tipos de cuentas")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            )
    })
    @GetMapping("/accounts")
    public ResponseEntity<PageResponse<OutputAccountType>> getAccountTypes(
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(required = false) Optional<Double> interest,
            @RequestParam(required = false) Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
    ) {
        log.info("Buscando todos los tipos de cuentas");
        // Ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Paginación
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<OutputAccountType> pageResult = accountTypeService.findAll(isDeleted, name, interest, PageRequest.of(page, size, sort))
                .map(AccountTypeMapper::toOutputAccountType);
        return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Crea un nuevo tipo de cuenta.
     *
     * @param newAccountType el producto que se quiere crear
     * @return Un {@link ResponseEntity} con la información del tipo de cuenta creada dentro de un objeto {@link OutputAccountType}
     * @throws ProductAlreadyExistsException si ya existe un tipo de cuenta con ese mismo nombre
     */
    @Operation(description = "Crea un tipo de cuenta")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputAccountType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict"
            )
    })
    @PostMapping("/accounts")
    public ResponseEntity<OutputAccountType> createAccountType(@RequestBody @Valid NewAccountType newAccountType) {
        log.info("Creando un nuevo tipo de cuenta");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        AccountTypeMapper.toOutputAccountType(
                                accountTypeService.save(newAccountType)
                        )
        );
    }

    /**
     * Recupera un tipo de cuenta por su ID.
     *
     * @param id el ID del tipo del tipo de cuenta que se quiere recuperar
     * @return Un {@link ResponseEntity} con la información del tipo de cuenta encontrada dentro de un objeto {@link OutputAccountType}
     * @throws ProductDoesNotExistException si el producto con ese id no existe
     */
    @Operation(description = "Obtiene un tipo de cuenta por ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputAccountType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @GetMapping("accounts/{id}")
    public ResponseEntity<OutputAccountType> getAccountTypeById(@PathVariable String id) {
        log.info("Buscando un tipo de cuenta por ID {}", id);
        return ResponseEntity.ok(
                AccountTypeMapper.toOutputAccountType(
                        accountTypeService.findById(id)
                )
        );
    }

    /**
     * Recupera un tipo de cuenta por su nombre.
     *
     * @param name el nombre del producto que se quiere recuperar
     * @return Un {@link ResponseEntity} con la información del tipo de cuenta encontrada dentro de un objeto {@link OutputAccountType}
     * @throws ProductDoesNotExistException si no existe ningún tipo de cuenta con ese nombre
     */
    @Operation(description = "Obtiene un tipo de cuenta por nombre")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputAccountType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @GetMapping("accounts/name/{name}")
    public ResponseEntity<OutputAccountType> getAccountTypeByName(@PathVariable String name) {
        log.info("Buscando un tipo de cuenta con nombre {}", name);
        return ResponseEntity.ok(
                AccountTypeMapper.toOutputAccountType(
                        accountTypeService.findByName(name)
                )
        );
    }

    /**
     * Actualiza un tipo de cuenta por su ID.
     *
     * @param id el ID del tipo de cuenta que se quiere actualizar
     * @param updatedAccountType los cambios que se desean aplicar al tipo de cuenta
     * @return Un {@link ResponseEntity} con la información del tipo de cuenta actualizado dentro de un objeto {@link OutputAccountType}
     * @throws ProductDoesNotExistException si el tipo de cuenta con ese Id no existe
     * @throws ProductAlreadyExistsException si ya existe un tipo de cuenta con ese nombre
     */
    @Operation(description = "Actualiza un tipo de cuenta por ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputAccountType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict"
            )
    })
    @PutMapping("/accounts/{id}")
    public ResponseEntity<OutputAccountType> updateAccountType(
            @PathVariable String id,
            @RequestBody @Valid UpdatedAccountType updatedAccountType
    ) {
        log.info("Actualizando un tipo de cuenta por ID {}", id);
        return ResponseEntity.ok(
                AccountTypeMapper.toOutputAccountType(
                        accountTypeService.update(id, updatedAccountType)
                )
        );
    }

    /**
     * Elimina un tipo de cuenta por su ID.
     *
     * @param id el ID del producto que se quiere eliminar
     * @return Un {@link ResponseEntity} con la información del tipo de cuenta actualizado dentro de un objeto {@link OutputAccountType}
     * @throws ProductDoesNotExistException si el tipo de cuenta con ese Id no existe
     */
    @Operation(description = "Elimina un tipo de cuenta por ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OutputAccountType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<OutputAccountType> deleteAccountType(
            @PathVariable String id
    ) {
        log.info("Borrando un producto por ID {}", id);
        return ResponseEntity.ok(
                AccountTypeMapper.toOutputAccountType(
                        accountTypeService.delete(id)
                )
        );
    }

    /**
     * Importa tipos de cuentas desde un archivo CSV.
     *
     * @param file el archivo CSV con los tipos de cuentas a importar
     * @return Un {@link ResponseEntity} con un mensaje de éxito si se ha importado correctamente o un error en caso contrario
     */
    @Operation(description = "Importa tipos de cuentas desde un archivo CSV")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "OK"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request"
            )
    })
    @PostMapping("/accounts/import")
    public ResponseEntity<List<OutputAccountType>> importAccountTypes(@RequestParam("file") MultipartFile file) {
        log.info("Importando tipos de cuentas desde un archivo");
        File tempFile = null;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), ".csv");
            file.transferTo(tempFile);
            LinkedList<NewAccountType> list = new LinkedList<>(); // Lista de categorias
            AtomicReference<Boolean> somethingWentWrong = new AtomicReference<>(false);

            accountTypeStorageCSV.read(tempFile)
                    .doOnNext(category -> {
                        if (category == null) somethingWentWrong.set(true);
                        else list.add(category);
                    }).subscribe();
            tempFile.delete();
            // Si alguna no se ha podido importar
            Boolean result = somethingWentWrong.get();
            if (result) throw new ProductStorageException("Error al importar tipos de cuentas desde un CSV");
            // Ver si ninguna existe en la base de datos
            AccountType existingAccountType = null;
            for (NewAccountType newAccountType : list) {
                try {
                    existingAccountType = accountTypeService.findByName(newAccountType.getName());
                } catch (ProductDoesNotExistException ignored) {
                }
            }
            if (existingAccountType != null)
                throw new ProductAlreadyExistsException("La categoría con nombre: " + existingAccountType.getName() + " ya existe");
            // Guardar las categorías en la base de datos
            List<OutputAccountType> saved = new LinkedList<>();
            for (NewAccountType newAccountType : list) {
                saved.add(
                        AccountTypeMapper.toOutputAccountType(
                                accountTypeService.save(newAccountType)
                        )
                );
            }
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            log.error("Error al importar tipos de cuentas", e);
            throw new ProductStorageException("Error al importar tipos de cuentas desde un CSV");
        } finally {
            try {
                tempFile.delete();
            } catch (Exception e) {
                log.error("Error al borrar el archivo temporal", e);
            }
        }
    }

    /**
     * Recupera una lista de tipos de tarjetas basada en los filtros proporcionados y parámetros de paginación.
     *
     * @param name        El nombre del tipo de cuenta por el que se quiere filtrar la lista
     * @param isDeleted   Sí se filtra por productos eliminados.
     * @param page        El número de página para la paginación.
     * @param size        El número de productos por página.
     * @param sortBy      El campo por el que se ordena.
     * @param direction   La dirección de ordenamiento (asc o desc).
     * @param request     La solicitud HTTP.
     * @return Un {@link ResponseEntity} que contiene un {@link PageResponse} de objetos {@link OutputCardType}.
     */
    @Operation(description = "Recupera una lista de tipos de tarjetas basada en los filtros proporcionados y parámetros de paginación")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PageResponse.class)
                    )
            )
    })
    @GetMapping("/cards")
    public ResponseEntity<PageResponse<OutputCardType>> getCardTypes(
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(required = false) Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request
    ) {
        log.info("Buscando todos los tipos de targetas");
        // Ordenación
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        // Paginación
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString());
        Page<OutputCardType> pageResult = cardTypeService.findAll(isDeleted, name, PageRequest.of(page, size, sort))
                .map(CardTypeMapper::toOutputCardType);
        return ResponseEntity.ok()
                .header("link", paginationLinksUtils.createLinkHeader(pageResult, uriBuilder))
                .body(PageResponse.of(pageResult, sortBy, direction));
    }

    /**
     * Crea un nuevo tipo de tarjeta.
     *
     * @param newCardType el nuevo tipo de tarjeta que se quiere crear.
     * @return Un {@link ResponseEntity} con la información del tipo de tarjeta creado dentro de un objeto {@link OutputCardType}.
     * @throws ProductAlreadyExistsException si ya existe un tipo de tarjeta con ese nombre ya existe.
     */
    @Operation(description = "Crea un nuevo tipo de tarjeta")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = OutputCardType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict"
            )
    })
    @PostMapping("/cards")
    public ResponseEntity<OutputCardType> createCardType(@RequestBody @Valid NewCardType newCardType) {
        log.info("Creando un nuevo producto");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CardTypeMapper.toOutputCardType(
                                cardTypeService.save(newCardType)
                        )
        );
    }

    /**
     * Recupera un tipo de tarjeta por su ID.
     *
     * @param id el ID del producto que se quiere recuperar.
     * @return Un {@link ResponseEntity} con la información del tipo de tarjeta encontrado dentro de un objeto {@link OutputCardType}.
     * @throws ProductDoesNotExistException si el tipo de tarjeta con ese Id no existe
     */
    @Operation(description = "Recupera un tipo de tarjeta por su ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = OutputCardType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @GetMapping("cards/{id}")
    public ResponseEntity<OutputCardType> getCardType(@PathVariable String id) {
        log.info("Buscando un producto por ID {}", id);
        return ResponseEntity.ok(
                CardTypeMapper.toOutputCardType(
                        cardTypeService.findById(id)
                )
        );
    }

    /**
     * Recupera un tipo de tarjeta por su nombre.
     *
     * @param name el nombre del tipo de tarjeta que se quiere recuperar
     * @return Un {@link ResponseEntity} con la información del tipo de tarjeta encontrado dentro de un objeto {@link OutputCardType}.
     * @throws ProductDoesNotExistException si el tipo de cuenta con ese nombre no existe
     */
    @Operation(description = "Recupera un tipo de tarjeta por su nombre")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = OutputCardType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @GetMapping("cards/name/{name}")
    public ResponseEntity<OutputCardType> getCardTypeByName(@PathVariable String name) {
        log.info("Buscando un tipo de tarjeta con nombre {}", name);
        return ResponseEntity.ok(
                CardTypeMapper.toOutputCardType(
                        cardTypeService.findByName(name)
                )
        );
    }

    /**
     * Actualiza un producto por su ID.
     *
     * @param id el ID del producto que se quiere actualizar
     * @return Un {@link ResponseEntity} con la información del tipo de tarjeta actualizado dentro de un objeto {@link OutputCardType}.
     * @throws ProductDoesNotExistException si el tipo de cuenta con ese Id no existe
     * @throws ProductAlreadyExistsException si ya existe un tipo de tarjeta con ese nombre ya existe.
     */
    @Operation(description = "Actualiza un tipo de tarjeta por su ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = OutputCardType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @PutMapping("/cards/{id}")
    public ResponseEntity<OutputCardType> updateCardById(
            @PathVariable String id,
            @RequestBody @Valid UpdatedCardType updatedCardType
    ) {
        log.info("Actualizando un producto por ID {}", id);
        return ResponseEntity.ok(
                CardTypeMapper.toOutputCardType(
                        cardTypeService.update(id, updatedCardType)
                )
        );
    }

    /**
     * Elimina un tipo de tarjeta por su ID.
     *
     * @param id el ID del tipo de tarjeta que se quiere eliminar
     * @return Un {@link ResponseEntity} con la información del tipo de tarjeta actualizado dentro de un objeto {@link OutputCardType}.
     * @throws ProductDoesNotExistException si el tipo de cuenta con ese Id no existe
     */
    @Operation(description = "Elimina un tipo de tarjeta por su ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = OutputCardType.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found"
            )
    })
    @DeleteMapping("/cards/{id}")
    public ResponseEntity<OutputCardType> deleteCardType(
            @PathVariable String id
    ) {
        log.info("Borrando un tipo de tarjeta por ID {}", id);
        return ResponseEntity.ok(
                CardTypeMapper.toOutputCardType(
                        cardTypeService.delete(id)
                )
        );
    }
}
