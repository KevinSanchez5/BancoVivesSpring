package vives.bancovives.rest.movements.model;

public enum MovementType {
    TRANSFERENCIA, // requiere cuenta de referencia y cuenta de destino
    INTERESMENSUAL, // requiere cuenta de referencia
    NOMINA, // requiere cuenta de referencia
    PAGO,  // requiere tarjeta y cuenta de referencia
    INGRESO,  // requiere tarjeta y cuenta de referencia
    EXTRACCION; // requiere tarjeta y cuenta de referencia

    public boolean requiresCard() {
        return this == PAGO || this == INGRESO || this == EXTRACCION;
    }

    public boolean requiresDestinationAccount() {
        return this == TRANSFERENCIA;
    }
}
