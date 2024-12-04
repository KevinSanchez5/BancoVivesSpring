package vives.bancovives.rest.accounts.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vives.bancovives.rest.accounts.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByPublicId(String id);
    Optional<Account> findByIban(String iban);
    List<Account> findAllByAccountType_InterestNotNull();
}
