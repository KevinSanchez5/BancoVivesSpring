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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@CacheConfig(cacheNames = {"accounts"})

public class AccountServiceImpl  implements AccountService {
    
    private final AccountRepository repository;
    private final ClientRepository clientRepository;
    private final AccountTypeRepository accountTypeRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository, ClientRepository clientRepository, AccountTypeRepository accountTypeRepository) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.accountTypeRepository = accountTypeRepository;
    }

    @Override
    public Page<Account> findAll(
            Optional<String> iban,
            Optional<Boolean> isDeleted,
            Pageable pageable

    ){
        log.info(("Buscando todas las cuentas"));
        Specification<Account> ibanSpec= ((root, query, criteriaBuilder) ->
                iban.map(m->criteriaBuilder.like(criteriaBuilder.lower(root.get("iban")), "%" + m.toLowerCase() + "%"))
                        .orElseGet(()->criteriaBuilder.isTrue(criteriaBuilder.literal(true))));

        Specification<Account> isDeletedSpec= ((root, query, criteriaBuilder) ->
                isDeleted.map(m->criteriaBuilder.equal(root.get("isDeleted"), m))
                        .orElseGet(()->criteriaBuilder.isTrue(criteriaBuilder.literal(true))));

        Specification<Account> criterio=Specification.where(ibanSpec)
                .and(isDeletedSpec);
        return repository.findAll(criterio, pageable);
    }

    @Override
    @Cacheable(key = "#id")
    public Account findById(String id) {
        log.info("Buscando cuenta por id: " + id);
        return existsAccountByPublicId(id);
    }

    @Override
    public Account findByIban(String iban) {
        log.info("Buscando cuenta por iban: {}", iban);
        return repository.findByIban(iban).orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con iban " + iban));
    }
    @Override
    @CachePut(key = "#result.id")
    public Account save (InputAccount inputAccount){
        log.info("Guardando cuenta");
        Client client = existClientByDniAndValidated(inputAccount.getDni());
        AccountType accountType= accountTypeRepository.findByName(inputAccount.getAccountType())
                .orElseThrow(()-> new AccountNotFoundException("Tipo de cuenta no encontrado"));
        Account mappedAccount = AccountMapper.toAccount(inputAccount, accountType, client);
        if(repository.findByIban(mappedAccount.getIban()).isPresent()) {
            throw new AccountConflictException("La cuenta con iban " + mappedAccount.getIban() + " ya existe");
        }
        return repository.save(mappedAccount);
    }

    @Override
    @CacheEvict(key = "#id")
    public Account deleteById(String id){
        log.info("Eliminando cuenta con el id" + id );
        Account account = existsAccountByPublicId(id);
        account.setDeleted(true);
        account.setUpdatedAt(LocalDateTime.now());
        return repository.save(account);
    }

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
            AccountType accountType = accountTypeRepository.findByName(updatedAccount.getAccountType())
                    .orElseThrow(() -> new RuntimeException("Account type not found"));
            existingAccount.setAccountType(accountType);
        }
        return repository.save(existingAccount);
    }

    public Account existsAccountByPublicId(String id){
        return repository.findByPublicId(id).orElseThrow(()-> new AccountNotFoundException("Cuenta no encontrada con id " + id));
    }

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

    public void sameDniInputAndSaved(String dni, Account account){
        if(!dni.equals(account.getClient().getDni())){
            throw new ClientBadRequest("El dni del cliente no coincide con el dni del cliente de la cuenta");
        }
    }

}
