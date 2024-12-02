package vives.bancovives.rest.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vives.bancovives.rest.cards.model.Card;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardsRepository extends JpaRepository<Card, String>, JpaSpecificationExecutor<Card> {
    Optional<Card> findByCardOwner(String owner);
    Optional<Card> findByPublicId(String id);
    Optional<Card> findByCardNumber(String cardNumber);
}