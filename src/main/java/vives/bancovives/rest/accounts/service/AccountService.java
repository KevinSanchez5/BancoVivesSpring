package vives.bancovives.rest.accounts.service;

import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.dto.input.InputAccount;


import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    Page<Account> findAll(
        Optional<String> iban,
        Optional<Boolean> isDeleted,
        Pageable pageable
    );
    Account findById(UUID id);
    Account findByIban(String iban);
    Account save(InputAccount account);
    Account deleteById(UUID id);
    Account updateById(UUID id, InputAccount updatedAccount);

}
