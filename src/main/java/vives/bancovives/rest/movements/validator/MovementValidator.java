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
import vives.bancovives.rest.movements.mapper.MovementMapper;
import vives.bancovives.rest.movements.model.MovementType;

@Component
public class MovementValidator {

    private final AccountRepository accountRepository;
    private final CardsRepository cardRepository;

    @Autowired
    public MovementValidator(AccountRepository accountRepository, CardsRepository cardRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
    }


    public void validateMovementDto(MovementCreateDto movementCreateDto) {
        validatePositiveAmount(movementCreateDto.getAmount());
        MovementType movementType = validateMovementType(movementCreateDto.getMovementType());

        Account accountOfReference = getAccountIfExists(movementCreateDto.getIbanOfReference());
        Account accountOfDestination = movementType.requiresDestinationAccount()? getAccountIfExists(movementCreateDto.getIbanOfDestination()) : null;
        Card card = movementType.requiresCard() ? getCardIfExists(movementCreateDto.getCardNumber()) : null;

        validateCorrectAttributesFromMovementType(movementType, movementCreateDto, accountOfReference, accountOfDestination, card);
    }

    public MovementType validateMovementType(String movementType){
        try{
            return MovementType.valueOf(movementType.trim().toUpperCase());
        }catch (IllegalArgumentException e){
            throw new MovementBadRequest("Debe insertar un tipo valido de movimiento (TRANSFERENCIA, INTERESMENSUAL, NOMINA, PAGO, INGRESO, EXTRACCION)");
        }
    }

    public void validateCorrectAttributesFromMovementType(MovementType movementType, MovementCreateDto dto, Account accountOfReference, Account accountOfDestination, Card card) {
        switch (movementType){
            case TRANSFERENCIA:
                validateTransferencia(dto, accountOfReference, accountOfDestination);
                break;
            case INTERESMENSUAL:
            case NOMINA:
                validateInteresMensualNomina(dto, accountOfReference);
                break;
            case INGRESO:
                validateIngreso(dto, accountOfReference, card);
                break;
            case PAGO:
            case EXTRACCION:
                validatePagoExtraccion(dto, accountOfReference, card);
                break;
            default: throw new MovementBadRequest("Debe insertar un tipo valido de movimiento");
        }
    }

    private void validateTransferencia(MovementCreateDto dto, Account accountOfReference, Account accountOfDestination) {
        if (accountOfReference == null || accountOfDestination == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia (origen) y una cuenta de destino para este tipo de movimiento");
        }
        validateSpentAmount(accountOfReference.getBalance(), dto.getAmount());
    }

    private void validateInteresMensualNomina(MovementCreateDto dto, Account accountOfReference) {
        if (accountOfReference == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia para este tipo de movimiento");
        }
    }

    private void  validateIngreso(MovementCreateDto dto, Account accountOfReference, Card card) {
        if (accountOfReference == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia para este tipo de movimiento");
        }
        if (card == null) {
            throw new MovementBadRequest("Se necesita una tarjeta para este tipo de movimiento");
        }
    }

    private void validatePagoExtraccion(MovementCreateDto dto, Account accountOfReference, Card card) {
        if (accountOfReference == null) {
            throw new MovementBadRequest("Se necesita una cuenta de referencia para este tipo de movimiento");
        }
        if (card == null) {
            throw new MovementBadRequest("Se necesita una tarjeta para este tipo de movimiento");
        }
        validateSpentAmount(accountOfReference.getBalance(), dto.getAmount());
        validateTimelyAmount(dto.getAmount(), card);
    }

    private Account getAccountIfExists(String iban) {
        if (iban != null) {
            return accountRepository.findByIban(iban).orElseThrow(() ->
                    new AccountNotFoundException("La cuenta con el IBAN " + iban + " no existe"));
        }
        return null;
    }

    private Card getCardIfExists(String cardNumber) {
        if (cardNumber != null) {
            return cardRepository.findByCardNumber(cardNumber).orElseThrow(() ->
                    new CardDoesNotExistException("La tarjeta con el numero " + cardNumber + " no existe"));
        }
        return null;
    }


    public void validateSpentAmount(double accountAmount, double amountToSpend){
        if (amountToSpend > accountAmount) {
            throw new MovementBadRequest("La cantidad a gastar excede le dinero en la cuenta");
        }
    }

    public void validatePositiveAmount(double amount){
        if(amount <= 0){
            throw new MovementBadRequest("La cantidad a mover debe ser mayor a 0");
        }
    }

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
}
