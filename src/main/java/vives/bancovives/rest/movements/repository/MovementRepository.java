package vives.bancovives.rest.movements.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vives.bancovives.rest.movements.model.Movement;

@Repository
public interface MovementRepository extends MongoRepository<Movement, ObjectId>, MovementRepositoryCustom {
}
