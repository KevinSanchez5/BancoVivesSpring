package vives.bancovives.rest.products.accounttype.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vives.bancovives.rest.products.accounttype.model.AccountType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, UUID>, JpaSpecificationExecutor<AccountType> {
    Optional<AccountType> findByPublicId(String id);
    Optional<AccountType> findByName(String name);
}
