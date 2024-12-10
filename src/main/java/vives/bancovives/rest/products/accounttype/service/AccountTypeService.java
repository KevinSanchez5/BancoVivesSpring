package vives.bancovives.rest.products.accounttype.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.dto.input.UpdatedAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;

import java.util.Optional;

public interface AccountTypeService {
    Page<AccountType> findAll(
        Optional<Boolean> isDeleted,
        Optional<String> name,
        Optional<Double> interest,
        Pageable pageable
    );
    AccountType findById(String id);
    AccountType findByName(String name);
    AccountType save(NewAccountType newAccountType);
    AccountType delete(String id);
    AccountType update(String id, UpdatedAccountType updatedProduct);
}
