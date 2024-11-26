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
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.repositories.AccountTypeRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@CacheConfig(cacheNames = {"accounts"})

public class AccountServiceImpl  implements AccountService{
    private final AccountRepository repository;
    private final AccountTypeRepository accountTypeRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository , AccountTypeRepository accountTypeRepository, AccountRepository accountRepository) {

        this.repository = repository;
        this.accountTypeRepository = accountTypeRepository;
        this.accountRepository = accountRepository;
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
        log.info("Buscando cuenta por id: {}", id);
        return repository.findByPublicId(id).orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con id " + id));
    }

    @Override
    public Account findByIban(String iban) {
        log.info("Buscando cuenta por iban: {}", iban);
        return repository.findByIban(iban).orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con iban " + iban));
    }
    @Override
    @CachePut(key = "#result.id")
   public Account save (InputAccount inputAccount){
        log.info("Creando nueva cuenta");
        AccountType accountType= accountTypeRepository.findByName(inputAccount.getAccountType())
                .orElseThrow(()-> new AccountNotFoundException("Tipo de cuenta no encontrado"));
        Account mappedAccount= AccountMapper.toAccount(inputAccount, accountType);
        if(accountRepository.findByIban(mappedAccount.getIban()).isPresent()){
            throw new AccountConflictException("Cuenta con iban " + mappedAccount.getIban() + " ya existe");
        }
        return accountRepository.save(mappedAccount);
    }

    @Override
    @CacheEvict(key = "#id")
    public Account deleteById(UUID id){
        log.info("Eliminando cuenta con el id{}", id);
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
        log.info("Actualizando la cuenta con id {}", id);

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con id " + id));
        if(updatedAccount.getPassword() != null){
            existingAccount.setPassword(updatedAccount.getPassword());
        }
        if(updatedAccount.getAccountType() !=null){
            AccountType accountType = accountTypeRepository.findByName(updatedAccount.getAccountType())
                    .orElseThrow(() -> new RuntimeException("Account type not found"));
            existingAccount.setAccountType(accountType);
        }
        existingAccount.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(existingAccount);
    }



}
