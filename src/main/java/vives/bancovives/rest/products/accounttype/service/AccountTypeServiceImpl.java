package vives.bancovives.rest.products.accounttype.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.dto.input.UpdatedAccountType;
import vives.bancovives.rest.products.accounttype.mappers.AccountTypeMapper;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.repositories.AccountTypeRepository;
import vives.bancovives.rest.products.exceptions.ProductAlreadyExistsException;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Esta clase implementa la lógica de negocio para gestionar los tipos de cuentas en el sistema.
 * Proporciona métodos para crear, actualizar, eliminar y recuperar tipos de cuentas,
 * incluyendo soporte para búsquedas con filtros y paginación.
 * Además, integra capacidades de caché para optimizar el rendimiento de las operaciones.
 *
 * @author Diego Novillo Luceño
 * @since 1.0.0
 */
@Service
@Slf4j
@CacheConfig(cacheNames = {"accountTypes"})
public class AccountTypeServiceImpl implements AccountTypeService {

    private final AccountTypeRepository repository;

    /**
     * Constructor de la clase AccountTypeServiceImpl.
     *
     * @param repository el repositorio utilizado para interactuar con la base de datos de tipos de cuenta
     */
    @Autowired
    public AccountTypeServiceImpl(AccountTypeRepository repository) {
        this.repository = repository;
    }

    /**
     * Recupera una lista paginada de {@link AccountType} que cumple con los criterios de búsqueda proporcionados.
     *
     * @param isDeleted   indica si se deben incluir solo tipos de cuenta eliminados o no
     * @param name        el nombre parcial o completo por el que se desea filtrar
     * @param interest    el interés máximo por el que se desea filtrar
     * @param pageable    parámetros de paginación y ordenamiento
     * @return una página de {@link AccountType} que coincide con los criterios de búsqueda
     */
    @Override
    public Page<AccountType> findAll(
            Optional<Boolean> isDeleted,
            Optional<String> name,
            Optional<Double> interest,
            Pageable pageable
    ) {
        log.info("Recuperando todos los tipos de cuentas con filtros y paginación");

        Specification<AccountType> nameSpec = (root, query, criteriaBuilder) ->
                name.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + n.toLowerCase() + "%"))
                        .orElse(criteriaBuilder.conjunction());

        Specification<AccountType> isDeletedSpec = (root, query, criteriaBuilder) ->
                isDeleted.map(d -> criteriaBuilder.equal(root.get("isDeleted"), d))
                        .orElse(criteriaBuilder.conjunction());

        Specification<AccountType> interestSpec = (root, query, criteriaBuilder) ->
                interest.map(i -> criteriaBuilder.lessThanOrEqualTo(root.get("interest"), i))
                        .orElse(criteriaBuilder.conjunction());

        Specification<AccountType> combinedSpec = Specification.where(nameSpec)
                .and(isDeletedSpec)
                .and(interestSpec);

        return repository.findAll(combinedSpec, pageable);
    }

    /**
     * Recupera un tipo de cuenta por su identificador único.
     *
     * @param id el identificador único del tipo de cuenta
     * @return el {@link AccountType} correspondiente
     * @throws ProductDoesNotExistException si no se encuentra un tipo de cuenta con el ID especificado
     */
    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    public AccountType findById(String id) {
        log.info("Recuperando tipo de cuenta con ID: {}", id);
        return repository.findByPublicId(id)
                .orElseThrow(() -> new ProductDoesNotExistException("El tipo de cuenta con ID " + id + " no existe."));
    }

    /**
     * Busca un tipo de cuenta por su nombre
     *
     * @param name el nombre del tipo de cuenta a validar
     * @throws ProductDoesNotExistException si no existe un tipo de cuenta con ese nombre
     */
    @Override
    @Cacheable(key = "#name", unless = "#result == null")
    public AccountType findByName(String name) {
        log.info("Buscando el producto con nombre: " + name);
        return repository.findByName(name.trim().toUpperCase()).orElseThrow(
                () -> new ProductDoesNotExistException("El producto con nombre: " + name + " no existe"));
    }

    /**
     * Valída que no exista un tipo de cuenta con el mismo nombre y un ID diferente.
     *
     * @param name el nombre del tipo de cuenta a validar
     * @param id   el identificador único del tipo de cuenta, o null si se está creando uno nuevo
     * @throws ProductAlreadyExistsException si ya existe un tipo de cuenta con el mismo nombre
     */
    private void validateUniqueAccountTypeName(String name, UUID id) {
        Optional<AccountType> existingAccountType = Optional.empty();
        if (name != null ) existingAccountType = repository.findByName(name.trim().toUpperCase());
        if (existingAccountType.isPresent() && (id == null || !existingAccountType.get().getId().equals(id))) {
            throw new ProductAlreadyExistsException("El tipo de cuenta con el nombre " + name + " ya existe.");
        }
    }

    /**
     * Guarda un nuevo tipo de cuenta en la base de datos.
     *
     * @param newAccountType los datos del nuevo tipo de cuenta a crear
     * @return el {@link AccountType} creado
     */
    @Override
    @CachePut(key = "#result.publicId", unless = "#result == null")
    public AccountType save(NewAccountType newAccountType) {
        log.info("Creando un nuevo tipo de cuenta: {}", newAccountType);
        AccountType mappedAccountType = AccountTypeMapper.toAccountType(newAccountType);
        validateUniqueAccountTypeName(mappedAccountType.getName(), null);
        return repository.save(mappedAccountType);
    }

    /**
     * Elimina un tipo de cuenta de manera lógica estableciendo su estado como eliminado.
     *
     * @param id el identificador único del tipo de cuenta a eliminar
     * @return el {@link AccountType} eliminado
     */
    @Override
    @Caching(evict = {
            @CacheEvict(key = "#id"),
            @CacheEvict(key = "#result.name")
    })
    public AccountType delete(String id) {
        log.info("Eliminando tipo de cuenta con ID: {}", id);
        AccountType accountTypeToDelete = findById(id);
        accountTypeToDelete.setIsDeleted(true);
        accountTypeToDelete.setUpdatedAt(LocalDateTime.now());
        return repository.save(accountTypeToDelete);
    }

    /**
     * Actualiza los datos de un tipo de cuenta existente.
     *
     * @param id el identificador único del tipo de cuenta a actualizar
     * @param updatedAccountType los datos actualizados
     * @return el {@link AccountType} actualizado
     */
    @Override
    @Caching(put = {
            @CachePut(key = "#id", unless = "#result == null"),
            @CachePut(key = "#result.name", unless = "#result == null")
    })
    public AccountType update(String id, UpdatedAccountType updatedAccountType) {
        log.info("Actualizando tipo de cuenta con ID: {}", id);
        AccountType existingAccountType = findById(id);
        validateUniqueAccountTypeName(updatedAccountType.getName(), existingAccountType.getId());
        AccountTypeMapper.updateAccountTypeFromInput(existingAccountType, updatedAccountType);
        return repository.save(existingAccountType);
    }
}
