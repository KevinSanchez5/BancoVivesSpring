package vives.bancovives.rest.accounts.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.dto.input.InputAccount;


import java.util.Optional;

public interface AccountService {
    Page<Account> findAll(
        Optional<String> iban,
        Optional<String> clientDni,
        Optional<String> accountTypeName,
        Optional<Boolean> isDeleted,
        Pageable pageable
    );
    Account findById(String id);
    Account findByIban(String iban);
    Account save(InputAccount account);
    Account deleteById(String id);
    Account updateById(String id, InputAccount updatedAccount);

}
