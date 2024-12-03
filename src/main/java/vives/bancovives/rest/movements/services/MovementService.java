package vives.bancovives.rest.movements.services;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;

import java.util.Optional;

public interface MovementService {

    Page<MovementResponseDto> findAll(
            Optional<String> movementType,
            Optional<String> iban,
            Optional<String> clientDni,
            Optional<String> fecha,
            Optional<Boolean> isDeleted,
            Pageable pageable);

    MovementResponseDto findById(ObjectId id);

    MovementResponseDto save(MovementCreateDto movementCreateDto);

    MovementResponseDto update(ObjectId id, MovementCreateDto movementCreateDto);

    Void deleteById(ObjectId id);

    Boolean cancelMovement(ObjectId id);
}
