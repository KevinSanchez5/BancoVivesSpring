package vives.bancovives.rest.movements.services;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.movements.dtos.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.MovementResponseDto;
import vives.bancovives.rest.movements.dtos.MovementUpdateDto;

import java.util.Optional;

@Service
public class MovementServiceImpl implements MovementService{
    @Override
    public Page<MovementResponseDto> findAll(
            Optional<String> movementType,
            Optional<String> iban,
            Optional<String> clientDni,
            Optional<String> fecha,
            Optional<Boolean> isDeleted,
            Pageable pageable) {
        return null;
    }

    @Override
    public MovementResponseDto findById(ObjectId id) {
        return null;
    }

    @Override
    public MovementResponseDto save(MovementCreateDto movementCreateDto) {
        return null;
    }

    @Override
    public MovementResponseDto update(ObjectId id, MovementUpdateDto movementCreateDto) {
        return null;
    }

    @Override
    public Void deleteById(ObjectId id) {
    return null;
    }

    @Override
    public Boolean cancelMovement(ObjectId id) {
        return null;
    }
}
