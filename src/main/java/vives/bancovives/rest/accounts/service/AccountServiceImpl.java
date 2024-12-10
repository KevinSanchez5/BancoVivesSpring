package vives.bancovives.rest.accounts.service;

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
import vives.bancovives.rest.accounts.dto.input.InputAccount;
import vives.bancovives.rest.accounts.exception.AccountConflictException;
import vives.bancovives.rest.accounts.exception.AccountNotFoundException;
import vives.bancovives.rest.accounts.mapper.AccountMapper;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.repository.ClientRepository;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.repositories.AccountTypeRepository;
import vives.bancovives.rest.products.exceptions.ProductDoesNotExistException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion del servicio para manejar cuentas.
 */
@Service
@Slf4j
@CacheConfig(cacheNames = {"accounts"})

public class AccountServiceImpl  implements AccountService {
    
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final AccountTypeRepository accountTypeRepository;
    /**
     * Constructor para AccountServiceImpl.
     *
     * @param accountRepository el repositorio de la cuenta
     * @param clientRepository el repositorio del cliente
     * @param accountTypeRepository el repositorio de tipo de cuenta
     */
    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, ClientRepository clientRepository, AccountTypeRepository accountTypeRepository) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.accountTypeRepository = accountTypeRepository;
    }
    /**
     * Encuentra todas las cuentas basadas en los criterios dados.
     *
     * @param iban el filtro opcional de IBAN
     * @param clientDni el filtro opcional de DNI del cliente
     * @param accountTypeName el filtro opcional de nombre del tipo de cuenta
     * @param isDeleted el filtro opcional de si está eliminado
     * @param pageable la información de paginación
     * @return una página de cuentas
     */
    @Override
    public Page<Account> findAll(
            Optional<String> iban,
            Optional<String> clientDni,
            Optional<String> accountTypeName,
            Optional<Boolean> isDeleted,
            Pageable pageable

    ){
        log.info(("Buscando todas las cuentas"));
        Specification<Account> ibanSpec= ((root, query, criteriaBuilder) ->
                iban.map(m->criteriaBuilder.like(criteriaBuilder.lower(root.get("iban")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(()->criteriaBuilder.isTrue(criteriaBuilder.literal(true))));

        Specification<Account> clientDniSpec = ((root, query, criteriaBuilder) ->
                clientDni.map(cli->criteriaBuilder.like(criteriaBuilder.lower(root.get("client").get("dni")), "%" + cli.toLowerCase() + "%"))
                        .orElseGet(()->criteriaBuilder.isTrue(criteriaBuilder.literal(true))));

        Specification<Account> accountTypeNameSpec = ((root, query, criteriaBuilder) ->
                accountTypeName.map(m->criteriaBuilder.like(criteriaBuilder.lower(root.get("accountType").get("name")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(()->criteriaBuilder.isTrue(criteriaBuilder.literal(true))));

        Specification<Account> isDeletedSpec= ((root, query, criteriaBuilder) ->
                isDeleted.map(m->criteriaBuilder.equal(root.get("isDeleted"), m))
                        .orElseGet(()->criteriaBuilder.isTrue(criteriaBuilder.literal(true))));

        Specification<Account> criterio=Specification.where(ibanSpec).and(clientDniSpec).and(accountTypeNameSpec)
                .and(isDeletedSpec);
        return accountRepository.findAll(criterio, pageable);
    }
    /**
     * Encuentra una cuenta por su ID.
     *
     * @param id el ID de la cuenta
     * @return la cuenta
     * @throws AccountNotFoundException si la cuenta no se encuentra
     */
    @Override
    @Cacheable(key = "#id")
    public Account findById(String id) {
        log.info("Buscando cuenta por id: " + id);
        return existsAccountByPublicId(id);
    }
    /**
     * Encuentra una cuenta por su IBAN.
     *
     * @param iban el IBAN de la cuenta
     * @return la cuenta
     * @throws AccountNotFoundException si la cuenta no se encuentra
     */
    @Override
    public Account findByIban(String iban) {
        log.info("Buscando cuenta por iban: {}", iban);
        return accountRepository.findByIban(iban).orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con iban " + iban));
    }
    /**
     * Guarda una nueva cuenta.
     *
     * @param inputAccount la cuenta a guardar
     * @return la cuenta guardada
     * @throws AccountConflictException si ya existe una cuenta con el mismo IBAN
     */
    @Override
    @CachePut(key = "#result.id")
    public Account save (InputAccount inputAccount){
        log.info("Guardando cuenta");
        Client client = existClientByDniAndValidated(inputAccount.getDni());
        AccountType accountType = existsAccountTypeByName(inputAccount.getAccountType());
        Account mappedAccount = AccountMapper.toAccount(inputAccount, accountType, client);
        if(accountRepository.findByIban(mappedAccount.getIban()).isPresent()) {
            throw new AccountConflictException("La cuenta con iban " + mappedAccount.getIban() + " ya existe");
        }
        return accountRepository.save(mappedAccount);
    }
    /**
     * Elimina una cuenta por su ID.
     *
     * @param id el ID de la cuenta
     * @return la cuenta eliminada
     * @throws AccountNotFoundException si la cuenta no se encuentra
     */
    @Override
    @CacheEvict(key = "#id")
    public Account deleteById(String id){
        log.info("Eliminando cuenta con el id" + id );
        Account account = existsAccountByPublicId(id);
        account.setDeleted(true);
        account.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }
    /**
     * Actualiza una cuenta por su ID.
     *
     * @param id el ID de la cuenta
     * @param updatedAccount la información actualizada de la cuenta
     * @return la cuenta actualizada
     * @throws AccountNotFoundException si la cuenta no se encuentra
     * @throws ClientBadRequest si el DNI del cliente no coincide
     */
    @CachePut(key = "#id")
    @Override
    public Account updateById(String id, InputAccount updatedAccount) {
        log.info("Actualizando la cuenta con id " + id);

        Account existingAccount = existsAccountByPublicId(id);
        Client client = existClientByDniAndValidated(updatedAccount.getDni());
        sameDniInputAndSaved(client.getDni(), existingAccount);

        if(updatedAccount.getPassword() != null){
            existingAccount.setPassword(updatedAccount.getPassword());
        }
        existingAccount.setUpdatedAt(LocalDateTime.now());

        if(updatedAccount.getAccountType() !=null){
            AccountType accountType = existsAccountTypeByName(updatedAccount.getAccountType());
            existingAccount.setAccountType(accountType);
        }
        return accountRepository.save(existingAccount);
    }

    public List<Account> findMyAccounts(Principal principal){
        log.info("Buscando su informacion");
        String username = principal.getName();
        return clientRepository.findByUser_Username(username)
                .orElseThrow(()->
                new ClientNotFound("Cliente no encontrado")).getAccounts();
    }
    /**
     * Verifica si una cuenta existe por su ID público.
     *
     * @param id el ID público de la cuenta
     * @return la cuenta
     * @throws AccountNotFoundException si la cuenta no se encuentra
     */
    public Account existsAccountByPublicId(String id){
        return accountRepository.findByPublicId(id).orElseThrow(()-> new AccountNotFoundException("Cuenta no encontrada con id " + id));
    }
    /**
     * Verifica si un tipo de cuenta existe por su nombre.
     *
     * @param name el nombre del tipo de cuenta
     * @return el tipo de cuenta
     * @throws ProductDoesNotExistException si el tipo de cuenta no se encuentra
     */
    public AccountType existsAccountTypeByName(String name){
        return accountTypeRepository.findByName(name.trim().toUpperCase()).orElseThrow(() -> new ProductDoesNotExistException("No existe cuenta con nombre " + name));
    }
    /**
     * Verifica si un cliente existe por su DNI y está validado.
     *
     * @param dni el DNI del cliente
     * @return el cliente
     * @throws ClientNotFound si el cliente no se encuentra o está eliminado
     * @throws ClientBadRequest si el cliente no está validado
     */
    public Client existClientByDniAndValidated(String dni){
        Client client = clientRepository.findByDniIgnoreCase(dni).orElseThrow(()->
                new ClientNotFound("Cliente no encontrado con dni " + dni));
        if(client.isDeleted()){
            throw new ClientNotFound("Cliente eliminado con dni " + dni);
        }
        if(!client.isValidated()){
            throw new ClientBadRequest("Los datos del cliente con dni " + dni + " no están validados");
        }
        return client;
    }
    /**
     * Verifica si el DNI del cliente de entrada coincide con el DNI del cliente guardado.
     *
     * @param dni el DNI del cliente
     * @param account la cuenta
     * @throws ClientBadRequest si los DNIs no coinciden
     */
    public void sameDniInputAndSaved(String dni, Account account){
        if(!dni.equals(account.getClient().getDni())){
            throw new ClientBadRequest("El dni del cliente no coincide con el dni del cliente de la cuenta");
        }
    }

}