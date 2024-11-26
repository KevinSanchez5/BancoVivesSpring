package vives.bancovives.rest.products.cardtype.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.products.cardtype.dto.input.NewCardType;
import vives.bancovives.rest.products.cardtype.dto.input.UpdatedCardType;
import vives.bancovives.rest.products.cardtype.mappers.CardTypeMapper;
import vives.bancovives.rest.products.cardtype.model.CardType;
import vives.bancovives.rest.products.cardtype.repositories.CardTypeRepository;
import vives.bancovives.rest.products.exceptions.ProductAlreadyExistsException;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementa la interfaz {@link CardTypeService}, proporcionando métodos para administrar tipos de tarjetas.
 *
 * @author Diego Novillo Luceño
 * @since 1.0.0
 */
@Service
@Slf4j
@CacheConfig(cacheNames = {"cardTypes"})
public class CardTypeServiceImpl implements CardTypeService {

    private final CardTypeRepository repository;

    /**
     * Constructor para ProductServiceImpl.
     *
     * @param repository el {@link CardTypeRepository} que se utilizará para operaciones de base de datos
     */
    @Autowired
    public CardTypeServiceImpl(CardTypeRepository repository) {
        this.repository = repository;
    }

    /**
     * Recupera una lista paginada de {@link CardType} basada en los criterios de búsqueda proporcionados.
     *
     * @param isDeleted   parámetro opcional para filtrar por estado de eliminación
     * @param name        parámetro opcional para filtrar por nombre
     * @param pageable    información de paginación
     * @return una página de objetos {@link CardType} que coinciden con los criterios de búsqueda
     */
    @Override
    public Page<CardType> findAll(
            Optional<Boolean> isDeleted,
            Optional<String> name,
            Pageable pageable

    ) {
        log.info("Buscando todos los tipos de targetas");
        // Criterio de búsqueda por nombre
        Specification<CardType> nameSpec = (root, query, criteriaBuilder) ->
                name.map(m -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        // Criterio de búsqueda por si el objeto está eliminado o no
        Specification<CardType> specIsDeleted = (root, query, criteriaBuilder) ->
                isDeleted.map(d -> criteriaBuilder.equal(root.get("isDeleted"), d))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<CardType> criterio = Specification.where(nameSpec)
                .and(specIsDeleted);

        return repository.findAll(criterio, pageable);
    }

    /**
     * Recupera un tipo de tarjeta por su ID.
     *
     * @param id el identificador del tipo de tarjeta
     * @return el objeto {@link CardType} con el id especificado
     * @throws ProductDoesNotExistException si no se encuentra
     */
    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    public CardType findById(String id) {
        log.info("Buscando el producto con id: " + id);
        return repository.findByPublicId(id).orElseThrow(
                () -> new ProductDoesNotExistException("El tipo de targeta con id: " + id + " no existe"));
    }

    /**
     * Recupera un tipo de tarjeta por su nombre.
     *
     * @param name el nombre del tipo de tarjeta
     * @return el objeto {@link CardType} con el nombre especificado
     * @throws ProductDoesNotExistException si no se encuentra
     */
    @Override
    @Cacheable(key = "#result.publicId", unless = "#result == null")
    public CardType findByName(String name) {
        log.info("Buscando el tipo de targeta con nombre: " + name);
        return repository.findByName(name.trim().toUpperCase()).orElseThrow(
                () -> new ProductDoesNotExistException("El tipo de targeta con nombre: " + name + " no existe"));
    }

    /**
     * Valída que el nombre del tipo de tarjeta sea único en el repositorio.
     * Si un tipo de tarjeta con el mismo nombre ya existe y su ID es diferente del ID proporcionado,
     * se lanza una {@link ProductAlreadyExistsException}.
     *
     * @param name el nombre del tipo de tarjeta a validar
     * @param id el ID del tipo de tarjeta que se está actualizando, o null si se está creando un nuevo tipo de tarjeta
     * @throws ProductAlreadyExistsException si un tipo de tarjeta con el mismo nombre ya existe y su ID es diferente
     */

    private void validateUniqueCardName(String name, UUID id) {
        Optional<CardType> existingAccountType = Optional.empty();
        if (name != null ) existingAccountType = repository.findByName(name.trim().toUpperCase());
        if (existingAccountType.isPresent() && (id == null || !existingAccountType.get().getId().equals(id))) {
            throw new ProductAlreadyExistsException("El tipo de cuenta con nombre: " + name + " ya existe");
        }
    }

    /**
     * Guarda un nuevo tipo de tarjeta en la base de datos después de validar que el nombre sea único.
     *
     * @param newAccountType el objeto {@link NewCardType} que se va a guardar
     * @return el objeto {@link CardType} guardado
     * @throws ProductAlreadyExistsException si un tipo de tarjeta con el mismo nombre ya existe
     */
    @Override
    @CachePut(key = "#result.publicId", unless = "#result == null")
    public CardType save(NewCardType newAccountType) {
        log.info("Guardando el tipo de targeta: " + newAccountType);
        CardType mappedCardType = CardTypeMapper.toCardType(newAccountType);
        validateUniqueCardName(mappedCardType.getName(), null);
        return repository.save(mappedCardType);
    }

    /**
     * Elimina un tipo de tarjeta de la base de datos.
     *
     * @param id el ID de la tarjeta
     * @return objeto {@link CardType} que se ha eliminado
     * @throws ProductDoesNotExistException si no se encuentra
     */
    @Override
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "#result.name")
    })
    public CardType delete(String id) {
        log.info("Eliminando el producto con id: " + id);
        CardType accountTypeToDelete = findById(id);
        accountTypeToDelete.setIsDeleted(true);
        accountTypeToDelete.setUpdatedAt(LocalDateTime.now());
        return repository.save(accountTypeToDelete);
    }

    /**
     * Actualiza un tipo de tarjeta existente en la base de datos después de validar que el nombre es único.
     *
     * @param id el identificador único del producto
     * @param updatedCardType el objeto {@link UpdatedCardType} actualizado
     * @return el objeto {@link CardType} actualizado
     * @throws ProductDoesNotExistException  si el tipo de tarjeta con ese ID no se encuentra
     * @throws ProductAlreadyExistsException si un tipo de tarjeta con el mismo nombre ya existe y su ID es diferente
     */
    @Override
    @Caching(put = {
            @CachePut(key = "#id", unless = "#result == null"),
            @CachePut(key = "#result.name", unless = "#result == null")
    })
    public CardType update(String id, UpdatedCardType updatedCardType) {
        log.info("Actualizando el producto con id: " + id);
        CardType existingCardType = findById(id);
        validateUniqueCardName(updatedCardType.getName(), existingCardType.getId());
        CardTypeMapper.updateCardTypeFromInput(existingCardType, updatedCardType);
        return repository.save(existingCardType);
    }
}
