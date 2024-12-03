package vives.bancovives.rest.movements.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import vives.bancovives.rest.movements.model.Movement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MovementRepositoryCustomImpl implements MovementRepositoryCustom {

    private final MongoTemplate mongoTemplate;


    @Autowired
    public MovementRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Movement> findAllByFilters(
            Optional<String> movementType,
            Optional<String> ibanOfReference,
            Optional<LocalDate> fecha,
            Optional<Boolean> isDeleted,
            Pageable pageable) {

        Query query = new Query();

        movementType.ifPresent(type -> query.addCriteria(Criteria.where("movementType").is(type)));
        ibanOfReference.ifPresent(iban -> query.addCriteria(Criteria.where("ibanOfReference").is(iban)));

        fecha.ifPresent(f -> {
            LocalDateTime startOfDay = f.atStartOfDay();
            LocalDateTime endOfDay = f.atTime(LocalTime.MAX);
            query.addCriteria(Criteria.where("fecha").gte(startOfDay).lte(endOfDay));
        });
        isDeleted.ifPresent(deleted -> query.addCriteria(Criteria.where("isDeleted").is(deleted)));

        long total = mongoTemplate.count(query, Movement.class);
        query.with(pageable);

        List<Movement> movements = mongoTemplate.find(query, Movement.class);

        return new PageImpl<>(movements, pageable, total);
    }
}
