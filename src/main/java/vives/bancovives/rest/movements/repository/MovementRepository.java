package vives.bancovives.rest.movements.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.movements.model.Movement;

import java.util.List;

@Repository
public interface MovementRepository extends MongoRepository<Movement, ObjectId>, MovementRepositoryCustom {
    List<Movement> findAllByAccountOfReference_Iban(String accountOfReference);
}
