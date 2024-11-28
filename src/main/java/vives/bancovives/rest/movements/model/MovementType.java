package vives.bancovives.rest.movements.model;

public enum MovementType {
    TRANSFERENCIA, //de cuenta a cuenta
    INTERESMENSUAL, // de banco o de nada  a cuenta
    BIZUM,  // de cuenta a cuetna a traves de numeros de telefono
    NOMINA, // de nada a cuenta
    PAGO,  // de cuenta a nada o a cuenta de tienda, DEBE TARJETA INVOLUCRADA
    INGRESO,  // de cajero o de nada a cuenta, DEBE TENER TARJETA
    EXTRACCION // de cuenta a nada o a cajero, DEBE TENER TARJETA
}
