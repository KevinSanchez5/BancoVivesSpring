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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@CacheConfig(cacheNames = {"accounts"})

public class AccountServiceImpl  implements AccountService{
    private final AccountRepository repository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository) {
        this.repository = repository;
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
    public Account findById(UUID id) {
        log.info("Buscando cuenta por id: " + id);
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con id " + id));
    }

    @Override
    public Account findByIban(String iban) {
        log.info("Buscando cuenta por iban: " + iban);
        return repository.findByIban(iban).orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con iban " + iban));
    }
    @Override
    @CachePut(key = "#result.id")
    public Account save (InputAccount account){
        log.info("Guardando cuenta"+ account);
        Account mappedAccount = AccountMapper.toAccount(account);
        if(repository.findByIban(mappedAccount.getIban()).isPresent()) throw new AccountConflictException("La cuenta con iban " + mappedAccount.getIban() + " ya existe");
        return repository.save(mappedAccount);
    }

    @Override
    @CacheEvict(key = "#id")
    public Account deleteById(UUID id){
        log.info("Eliminando cuenta con el id" + id );
        Optional <Account> account = repository.findById(id);
        if(account.isPresent()){
            Account accountToDelete = account.get();
            accountToDelete.setDeleted(true);
            accountToDelete.setUpdatedAt(LocalDateTime.now());
            return repository.save(accountToDelete);
        }else{
            throw new AccountNotFoundException("Cuenta no encontrada con id " + id);
        }
    }

    @CachePut(key = "#id")
    @Override
    public Account updateById(UUID id, InputAccount updatedAccount) {
        log.info("Actualizando la cuenta con id " + id);

        Optional<Account> accountOptional = repository.findById(id);

        if (accountOptional.isPresent()) {
            Account existingAccount = accountOptional.get();

            // Actualizamos solo los campos que pueden modificarse
            existingAccount.setBalance(updatedAccount.getBalance());
            existingAccount.setUpdatedAt(LocalDateTime.now());

            // Guardamos los cambios
            return repository.save(existingAccount);
        } else {
            throw new AccountNotFoundException("Cuenta no encontrada con id " + id);
        }
    }



}
