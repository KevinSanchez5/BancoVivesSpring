package vives.bancovives.rest.movements.services;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.model.Movement;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface MovementService {

    Page<MovementResponseDto> findAll(
            Optional<String> movementType,
            Optional<String> iban,
            Optional<String> fecha,
            Optional<String> clientPublicId,
            Optional<String> clientDni,
            Optional<Boolean> isDeleted,
            Pageable pageable);

    MovementResponseDto findById(ObjectId id);

    MovementResponseDto save(Principal principal, MovementCreateDto movementCreateDto);

    MovementResponseDto update(ObjectId id, MovementCreateDto movementCreateDto);

    Void deleteById(ObjectId id);

    Boolean cancelMovement(Principal principal, ObjectId id);

    MovementResponseDto addInterest(MovementCreateDto movementCreateDto);

    Page<MovementResponseDto> findMyMovements(Principal principal, Pageable pageable);

}
