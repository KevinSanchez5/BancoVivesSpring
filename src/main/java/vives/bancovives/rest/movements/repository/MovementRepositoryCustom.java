package vives.bancovives.rest.movements.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.movements.model.Movement;

import java.time.LocalDate;
import java.util.Optional;

public interface MovementRepositoryCustom {
    Page<Movement> findAllByFilters(
            Optional<String> movementType,
            Optional<String> ibanOfReference,
            Optional<LocalDate> fecha,
            Optional<Boolean> isDeleted,
            Pageable pageable);
}
