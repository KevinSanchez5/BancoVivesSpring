package vives.bancovives.rest.movements.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vives.bancovives.rest.accounts.exception.AccountNotFoundException;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
import vives.bancovives.rest.cards.exceptions.CardDoesNotExistException;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.exceptions.MovementBadRequest;
import vives.bancovives.rest.movements.exceptions.MovementForbidden;
import vives.bancovives.rest.movements.model.MovementType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Clase que valida los campos de un movimiento.
 */
@Component
public class MovementValidator {

    private final AccountRepository accountRepository;
    private final CardsRepository cardRepository;

    @Autowired
    public MovementValidator(AccountRepository accountRepository, CardsRepository cardRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
    }

    /**
     * Método que valida los campos basicos de un movimiento como el tipo de movimiento
     * la cuenta de referencia y que el monto sea positivo.
     *
     *  @param movementCreateDto Objeto de la clase MovementCreateDto que se va a validar
     */
    public void validateMovementDto(MovementCreateDto movementCreateDto) {
        validatePositiveAmount(movementCreateDto.getAmount());
        MovementType movementType = validateMovementType(movementCreateDto.getMovementType());

        Account accountOfReference = getAccountIfExists(movementCreateDto.getIbanOfReference());
        Account accountOfDestination = movementType.requiresDestinationAccount()? getAccountIfExists(movementCreateDto.getIbanOfDestination()) : null;
        Card card = movementType.requiresCard() ? getCardIfExists(movementCreateDto.getCardNumber()) : null;

        validateCorrectAttributesFromMovementType(movementType, movementCreateDto, accountOfReference, accountOfDestination, card);
    }


    /**
     * Método que valida el tipo de movimiento.
     * @param movementType
     * @return MovementType
     */
    public MovementType validateMovementType(String movementType){
        try{
            return MovementType.valueOf(movementType.trim().toUpperCase());
        }catch (IllegalArgumentException e){
            throw new MovementBadRequest("Debe insertar un tipo valido de movimiento (TRANSFERENCIA, INTERESMENSUAL, NOMINA, PAGO, INGRESO, EXTRACCION)");
        }
    }

    /**
     * Método que valida los atributos correctos dependiendo del tipo de movimiento.
     * @param movementType Tipo de movimiento
     * @param dto        Objeto de la clase MovementCreateDto que se va a validar
     * @param accountOfReference Cuenta de referencia
     * @param accountOfDestination Cuenta de destino
     * @param card Tarjeta
     */
    public void validateCorrectAttributesFromMovementType(MovementType movementType, MovementCreateDto dto, Account accountOfReference, Account accountOfDestination, Card card) {
        switch (movementType){
            case TRANSFERENCIA:
                validateTransferencia(dto, accountOfReference, accountOfDestination);
                break;
            case NOMINA:
                validateNomina(dto, accountOfReference);
                break;
            case INGRESO:
                validateIngreso(dto, accountOfReference, card);
                break;
            case PAGO:
            case EXTRACCION:
                validatePagoExtraccion(dto, accountOfReference, card);
                break;
            case INTERESMENSUAL:
                throw new MovementForbidden("No tienes permisos para este tipo de movimiento");
            default: throw new MovementBadRequest("Debe insertar un tipo valido de movimiento");
        }
    }

    /**
     * Método que valida los campos de una transferencia. Se asegura que las dos cuentas realcionadas existan y sean validas
     * al igual que el dinero a gastar no exceda el dinero en la cuenta de referencia.
     * @param dto Objeto de la clase MovementCreateDto que se va a validar
     * @param accountOfReference Cuenta de referencia
     * @param accountOfDestination Cuenta de destino
     */
    private void validateTransferencia(MovementCreateDto dto, Account accountOfReference, Account accountOfDestination) {
        if (accountOfReference == null || accountOfDestination == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia (origen) y una cuenta de destino para este tipo de movimiento");
        }
        validateAccountIsNotDeleted(accountOfReference);
        validateAccountIsNotDeleted(accountOfDestination);
        validateSpentAmount(accountOfReference.getBalance(), dto.getAmount());
    }

    /**
     * Método que valida los campos de un interes mensual. Se asegura que la cuenta de referencia exista y sea valida
     * y que sy tipo de cuenta tenga intereses.
     * @param dto Objeto de la clase MovementCreateDto que se va a validar
     * @param accountOfReference Cuenta de referencia
     */
    public void validateInteresMensual(MovementCreateDto dto, Account accountOfReference) {
        if (accountOfReference == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia para este tipo de movimiento");
        }
        validateAccountIsNotDeleted(accountOfReference);
        if(accountOfReference.getAccountType().getInterest()<=0){
            throw new MovementBadRequest("El tipo de cuenta de la cuenta no tiene intereses");
        }
    }

    /**
     * Método que valida los campos de una nomina. Se asegura que la cuenta de referencia exista y sea valida
     * @param dto
     * @param accountOfReference
     */
    private void validateNomina(MovementCreateDto dto, Account accountOfReference) {
        if (accountOfReference == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia para este tipo de movimiento");
        }
        validateAccountIsNotDeleted(accountOfReference);
        validatePositiveAmount(dto.getAmount());
    }

    /**
     * Método que valida los campos de un ingreso. Se asegura que la cuenta de referencia y la tarjeta existan y sean validas
     * que la tarjeta pertenezca a la cuenta y que el monto no exceda los limites de la tarjeta.
     * @param dto
     * @param accountOfReference
     * @param card
     */
    private void  validateIngreso(MovementCreateDto dto, Account accountOfReference, Card card) {
        if (accountOfReference == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia para este tipo de movimiento");
        }
        if (card == null) {
            throw new MovementBadRequest("Se necesita una tarjeta para este tipo de movimiento");
        }
        validateAccountIsNotDeleted(accountOfReference);
        validateCardIsValid(card);
        validateCardAndAccountAreConnected(card, accountOfReference);
    }

    /**
     * Método que valida los campos de un pago o extraccion. Se asegura que la cuenta de referencia y la tarjeta existan y sean validas
     * que la tarjeta pertenezca a la cuenta y que el monto no exceda los limites de la tarjeta ni el dinero en la cuenta.
     * @param dto
     * @param accountOfReference
     * @param card
     */
    private void validatePagoExtraccion(MovementCreateDto dto, Account accountOfReference, Card card) {
        if (accountOfReference == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia para este tipo de movimiento");
        }
        if (card == null) {
            throw new MovementBadRequest("Se necesita una tarjeta para este tipo de movimiento");
        }
        validateAccountIsNotDeleted(accountOfReference);
        validateCardIsValid(card);
        validateSpentAmount(accountOfReference.getBalance(), dto.getAmount());
        validateCardAndAccountAreConnected(card, accountOfReference);
        validateTimelyAmount(dto.getAmount(), card);
    }

    /**
     * Método que obtiene una cuenta si existe.
     * @param iban IBAN de la cuenta
     * @return Account
     * @throws AccountNotFoundException Si la cuenta no existe
     */
    private Account getAccountIfExists(String iban) {
        if (iban != null) {
            return accountRepository.findByIban(iban).orElseThrow(() ->
                    new AccountNotFoundException("La cuenta con el IBAN " + iban + " no existe"));
        }
        return null;
    }

    /**
     * Método que obtiene una tarjeta si existe.
     * @param cardNumber Numero de la tarjeta
     * @return Card
     * @throws CardDoesNotExistException Si la tarjeta no existe
     */
    private Card getCardIfExists(String cardNumber) {
        if (cardNumber != null) {
            return cardRepository.findByCardNumber(cardNumber).orElseThrow(() ->
                    new CardDoesNotExistException("La tarjeta con el numero " + cardNumber + " no existe"));
        }
        return null;
    }


    /**
     * Método que valida que el monto a gastar no exceda el dinero en la cuenta.
     * @param accountAmount Dinero en la cuenta
     * @param amountToSpend Dinero a gastar
     */
    public void validateSpentAmount(double accountAmount, double amountToSpend){
        if (amountToSpend > accountAmount) {
            throw new MovementBadRequest("La cantidad a gastar excede le dinero en la cuenta");
        }
    }

    /**
     * Método que valida que el monto a mover sea positivo.
     * @param amount Monto a mover
     */
    public void validatePositiveAmount(double amount){
        if(amount <= 0){
            throw new MovementBadRequest("La cantidad a mover debe ser mayor a 0");
        }
    }

    /**
     * Método que valida que el monto a mover no exceda los limites de la tarjeta.
     * @param amount Monto a mover
     * @param card Tarjeta
     */
    public void validateTimelyAmount(double amount, Card card){
        if(amount + card.getSpentToday() > card.getDailyLimit()){
            throw new MovementBadRequest("La cantidad superaria el limite diario de la tarjeta");
        }
        if(amount + card.getSpentThisWeek() > card.getWeeklyLimit()){
            throw new MovementBadRequest("La cantidad superaria el limite semanal de la tarjeta");
        }
        if(amount + card.getSpentThisMonth() > card.getMonthlyLimit()){
            throw new MovementBadRequest("La cantidad superaria el limite mensual de la tarjeta");
        }
    }

    /**
     * Método que valida que la cuenta no este eliminada.
     * @param account Cuenta
     */
    public void validateAccountIsNotDeleted(Account account){
        if(account.isDeleted()){
            throw new MovementBadRequest("La cuenta esta eliminada");
        }
    }

    /**
     * Método que valida que la tarjeta no este eliminada, inactiva o caducada.
     * @param card Tarjeta
     */
    public void validateCardIsValid(Card card){
        if(card.getIsDeleted()){
            throw new MovementBadRequest("La tarjeta esta eliminada");
        }
        if(card.getIsInactive()){
            throw new MovementBadRequest("La tarjeta esta inactiva");
        }
        if(card.getExpirationDate() != null && cardExpirated(card.getExpirationDate())){
            throw new MovementBadRequest("La tarjeta esta caducada");
        }
    }

    /**
     * Método que valida que la fecha de expiración de la tarjeta no haya pasado.
     * @param expirationDate Fecha de expiración de la tarjeta
     * @return boolean
     */
    public boolean cardExpirated(String expirationDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        try {
            LocalDate parsedDate = LocalDate.parse("01/" + expirationDate, DateTimeFormatter.ofPattern("dd/MM/yy"));
            LocalDate firstDayOfNextMonth = parsedDate.plusMonths(1).withDayOfMonth(1);
            return LocalDate.now().isAfter(firstDayOfNextMonth);
        } catch (DateTimeParseException e) {
            throw new MovementBadRequest("La fecha de expiración de la tarjeta es inválida");
        }
    }

    /**
     * Método que valida que la tarjeta pertenezca a la cuenta.
     * @param card Tarjeta
     * @param account Cuenta
     */
    public void validateCardAndAccountAreConnected(Card card, Account account){
        if(!card.getAccount().getId().equals(account.getId())){
            throw new MovementBadRequest("La tarjeta no pertenece a la cuenta");
        }
    }
}