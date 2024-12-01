package vives.bancovives.rest.movements.services;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.accounts.exception.AccountNotFoundException;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
import vives.bancovives.rest.cards.exceptions.CardDoesNotExistException;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.dtos.input.MovementUpdateDto;
import vives.bancovives.rest.movements.exceptions.MovementNotFound;
import vives.bancovives.rest.movements.mapper.MovementMapper;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;
import vives.bancovives.rest.movements.repository.MovementRepository;
import vives.bancovives.rest.movements.validator.MovementValidator;

import java.util.Optional;

@Service
public class MovementServiceImpl implements MovementService{

    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;
    private final CardsRepository cardsRepository;
    private final MovementValidator validator;
    private final MovementMapper movementMapper;

    public MovementServiceImpl(MovementRepository movementRepository, AccountRepository accountRepository, CardsRepository cardRepository, MovementValidator movementValidator, MovementMapper movementMapper) {
        this.movementRepository = movementRepository;
        this.accountRepository = accountRepository;
        this.cardsRepository = cardRepository;
        this.validator = movementValidator;
        this.movementMapper = movementMapper;
    }
    @Override
    public Page<MovementResponseDto> findAll(
            Optional<String> movementType,
            Optional<String> iban,
            Optional<String> clientDni,
            Optional<String> fecha,
            Optional<Boolean> isDeleted,
            Pageable pageable) {

        return movementRepository.findAll(pageable).map(movementMapper::fromEntityToResponse);
    }

    @Override
    public MovementResponseDto findById(ObjectId id) {
        return movementMapper.fromEntityToResponse(existsMovementById(id));
    }

    @Override
    public MovementResponseDto save(MovementCreateDto movementCreateDto) {
        validator.validateMovementDto(movementCreateDto);

        Account accountOfReference = existsAccountByIban(movementCreateDto.getIbanOfReference());
        Account accountOfDestination = movementCreateDto.getIbanOfDestination() != null
                ? existsAccountByIban(movementCreateDto.getIbanOfDestination())
                : null;
        Card card = movementCreateDto.getCardNumber() != null
                ? existsCardByCardNumber(movementCreateDto.getCardNumber())
                : null;

        Movement movementToSave = movementMapper.fromCreateDtoToEntity(
                movementCreateDto, accountOfReference, accountOfDestination, card);

        moveMoney(movementToSave.getMovementType(), accountOfReference, accountOfDestination, card, movementCreateDto.getAmount());

        saveModificationsInAccountsAndCard(accountOfReference, accountOfDestination, card);
        return movementMapper.fromEntityToResponse(movementRepository.save(movementToSave));
    }

    @Override
    public MovementResponseDto update(ObjectId id, MovementCreateDto movementCreateDto) {
        Movement movementToUpdate = existsMovementById(id);

        if(movementToUpdate.getMovementType() != MovementType.TRANSFERENCIA){
            throw new UnsupportedOperationException("No se puede modificar un movimiento que no sea de tipo transferencia");
        }
        // Validar el nuevo movimiento
        validator.validateMovementDto(movementCreateDto);

        // Revertir la transferencia previa
        Account oldReferenceAccount = existsAccountByIban(movementToUpdate.getAccountOfReference().getIban());
        Account oldDestinationAccount = existsAccountByIban(movementToUpdate.getAccountOfDestination().getIban());

        oldReferenceAccount.setBalance(oldReferenceAccount.getBalance() + movementToUpdate.getAmountOfMoney());
        oldDestinationAccount.setBalance(oldDestinationAccount.getBalance() - movementToUpdate.getAmountOfMoney());

        // Validar las nuevas cuentas
        Account newReferenceAccount = existsAccountByIban(movementCreateDto.getIbanOfReference());
        Account newDestinationAccount = existsAccountByIban(movementCreateDto.getIbanOfDestination());

        // Actualizar la entidad del movimiento
        movementToUpdate.setAccountOfReference(newReferenceAccount);
        movementToUpdate.setAccountOfDestination(newDestinationAccount);
        movementToUpdate.setAmountOfMoney(movementCreateDto.getAmount());


        // Realizar la nueva transferencia
        moveMoney(
                MovementType.TRANSFERENCIA,
                newReferenceAccount,
                newDestinationAccount,
                null,
                movementCreateDto.getAmount()
        );

        // Guardar los cambios en las cuentas
        saveModificationsInAccountsAndCard(newReferenceAccount, newDestinationAccount, null);

        // Guardar el movimiento actualizado
        movementRepository.save(movementToUpdate);

        // Devolver el DTO de respuesta
        return movementMapper.fromEntityToResponse(movementToUpdate);
    }

    @Override
    public Void deleteById(ObjectId id) {
        Movement movementToDelete = existsMovementById(id);
        return null;
    }

    @Override
    public Boolean cancelMovement(ObjectId id) {
        Movement movementToCancel = existsMovementById(id);
        return null;
    }


    public Movement existsMovementById(ObjectId id){
        return movementRepository.findById(id).orElseThrow(
                () -> new MovementNotFound("Movimiento con id" + id + "no encontrado"));
    }

    public Account existsAccountByIban(String iban){
        return accountRepository.findByIban(iban).orElseThrow(
                () -> new AccountNotFoundException("Cuenta con iban" + iban + "no encontrada"));
    }

    public Card existsCardByCardNumber(String cardNumber){
        return cardsRepository.findByCardNumber(cardNumber).orElseThrow(
                () -> new CardDoesNotExistException("Tarjeta con numero" + cardNumber + "no encontrada"));
    }


    private void moveMoney(MovementType movementType, Account accountOfReference, Account accountOfDestination,Card card, Double amount){
        switch (movementType){
            case TRANSFERENCIA:
                accountOfReference.setBalance(accountOfReference.getBalance() - amount);
                accountOfDestination.setBalance(accountOfDestination.getBalance() + amount);
                break;
            case INGRESO, NOMINA:
                accountOfReference.setBalance(accountOfReference.getBalance() + amount);
                break;
            case PAGO:
            case EXTRACCION:
                accountOfReference.setBalance(accountOfReference.getBalance() - amount);
                setNewLimitsInCard(card, amount);
                break;
            case INTERESMENSUAL:
                accountOfReference.setBalance(accountOfReference.getBalance() + calculateInterest(accountOfReference));
        }
    }

    private Double calculateInterest(Account accountOfReference){
        return accountOfReference.getBalance() * accountOfReference.getAccountType().getInterest();
    }

    private void setNewLimitsInCard(Card card, Double amount){
        card.setSpentToday(card.getSpentToday() + amount);
        card.setSpentThisMonth(card.getSpentThisMonth() + amount);
        card.setSpentThisMonth(card.getSpentThisMonth() + amount);
    }

    private void saveModificationsInAccountsAndCard(Account accountOfReference, Account accountOfDestination, Card card){
        accountRepository.save(accountOfReference);
        if(accountOfDestination != null){
            accountRepository.save(accountOfDestination);
        }
        if(card != null){
            cardsRepository.save(card);
        }
    }
}
