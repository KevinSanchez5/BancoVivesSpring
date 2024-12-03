package vives.bancovives.rest.movements.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.movements.model.Movement;

import java.util.Optional;

public interface MovementRepositoryCustom {
    Page<Movement> findAllByFilters(
            Optional<String> movementType,
            Optional<String> ibanOfReference,
            Optional<String> fecha,
            Optional<Boolean> isDeleted,
            Pageable pageable);
}
