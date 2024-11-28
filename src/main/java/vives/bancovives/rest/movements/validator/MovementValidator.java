package vives.bancovives.rest.movements.validator;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.movements.exceptions.MovementBadRequest;
import vives.bancovives.rest.movements.model.Movement;

@Component
public class MovementValidator {


    public void validateMovement(Movement movement) {
        switch (movement.getMovementType()) {
            case TRANSFERENCIA:
            case BIZUM:
                if (movement.getAccountOfOrigin() == null || movement.getAccountOfDestination() == null) {
                    throw new MovementBadRequest("Se necesita una cuenta de origen y una cuenta de destino para este tipo de movimiento");
                }
                break;
            case INTERESMENSUAL:
            case NOMINA:
                if (movement.getAccountOfDestination() == null) {
                    throw new MovementBadRequest("Se necesita una cuenta de destino para este tipo de movimiento");
                }
                break;
            case PAGO:
            case INGRESO:
            case EXTRACCION:
                if (movement.getCard() == null || movement.getAccountOfOrigin() == null) {
                    throw new MovementBadRequest("Una tarjeta y una cuenta de origen son necesarias para este tipo de movimiento");
                }
                break;
            default: throw new MovementBadRequest("Debe insertar un tipo valido de movimiento");
        }
    }
}
