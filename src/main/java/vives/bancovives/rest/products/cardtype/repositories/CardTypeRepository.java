package vives.bancovives.rest.products.cardtype.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.cardtype.model.CardType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardTypeRepository extends JpaRepository<CardType, UUID>, JpaSpecificationExecutor<CardType> {
    Optional<CardType> findByPublicId(String id);
    Optional<CardType> findByName(String name);
}
