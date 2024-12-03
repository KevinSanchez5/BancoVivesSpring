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
import vives.bancovives.rest.movements.exceptions.MovementBadRequest;
import vives.bancovives.rest.movements.exceptions.MovementNotFound;
import vives.bancovives.rest.movements.mapper.MovementMapper;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;
import vives.bancovives.rest.movements.repository.MovementRepository;
import vives.bancovives.rest.movements.validator.MovementValidator;

import java.time.Duration;
import java.time.LocalDateTime;
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
            Optional<String> ibanOfReference,
            Optional<String> fecha,
            Optional<Boolean> isDeleted,
            Pageable pageable) {
        Page<Movement> movements = movementRepository.findAllByFilters(movementType, ibanOfReference, fecha, isDeleted, pageable);
        return movements.map(movementMapper::fromEntityToResponse);
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

        moveMoney(movementToSave);

        saveModificationsInAccountsAndCard(accountOfReference, accountOfDestination, card);
        return movementMapper.fromEntityToResponse(movementRepository.save(movementToSave));
    }

    @Override
    public MovementResponseDto update(ObjectId id, MovementCreateDto movementDto) {
        Movement movementToUpdate = existsMovementById(id);

        if(movementToUpdate.getMovementType() != MovementType.TRANSFERENCIA || !movementDto.getMovementType().trim().equalsIgnoreCase("TRANSFERENCIA")){
            throw new MovementBadRequest("No se puede modificar un movimiento que no sea de tipo transferencia");
        }
        validator.validateMovementDto(movementDto);

        Account oldReferenceAccount = existsAccountByIban(movementToUpdate.getAccountOfReference().getIban());
        Account oldDestinationAccount = existsAccountByIban(movementToUpdate.getAccountOfDestination().getIban());

        revertTransfer(oldReferenceAccount, oldDestinationAccount, movementToUpdate.getAmountOfMoney());

        Account newReferenceAccount = existsAccountByIban(movementDto.getIbanOfReference());
        Account newDestinationAccount = existsAccountByIban(movementDto.getIbanOfDestination());

        // Actualizar la entidad del movimiento
        movementToUpdate.setAccountOfReference(newReferenceAccount);
        movementToUpdate.setAccountOfDestination(newDestinationAccount);
        movementToUpdate.setAmountOfMoney(movementDto.getAmount());


        moveMoney(movementToUpdate);

        saveModificationsInAccountsAndCard(newReferenceAccount, newDestinationAccount, null);

        movementRepository.save(movementToUpdate);

        return movementMapper.fromEntityToResponse(movementToUpdate);
    }

    @Override
    public Void deleteById(ObjectId id) {
        Movement movementToDelete = existsMovementById(id);
        movementToDelete.setIsDeleted(true);
        movementRepository.save(movementToDelete);
        return null;
    }

    @Override
    public Boolean cancelMovement(ObjectId id) {
        Movement movementToCancel = existsMovementById(id);

        verifyIsATransferencia(movementToCancel.getMovementType());

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(movementToCancel.getCreatedAt(), now);

        if (duration.toHours() >= 24) {
            throw new MovementBadRequest("El movimiento no puede cancelarse porque han pasado más de 24 horas.");
        }

        revertTransfer(movementToCancel.getAccountOfReference(), movementToCancel.getAccountOfDestination(), movementToCancel.getAmountOfMoney());

        movementRepository.delete(movementToCancel);

        return true;
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


    private void moveMoney(Movement movement){
        switch (movement.getMovementType()){
            case TRANSFERENCIA:
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() - movement.getAmountOfMoney());
                movement.getAccountOfDestination().setBalance(movement.getAccountOfDestination().getBalance() + movement.getAmountOfMoney());
                break;
            case INGRESO, NOMINA:
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() + movement.getAmountOfMoney());
                break;
            case PAGO:
            case EXTRACCION:
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() - movement.getAmountOfMoney());
                setNewLimitsInCard(movement.getCard(), movement.getAmountOfMoney());
                break;
            case INTERESMENSUAL:
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() + calculateInterest(movement.getAccountOfReference()));
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

    private void revertTransfer(Account accountOfReference, Account accountOfDestination, Double amount){
        accountOfReference.setBalance(accountOfReference.getBalance() + amount);
        accountOfDestination.setBalance(accountOfDestination.getBalance() - amount);
        accountRepository.save(accountOfReference);
        accountRepository.save(accountOfDestination);
    }

    private void verifyIsATransferencia(MovementType movemntType){
        if(movemntType != MovementType.TRANSFERENCIA){
            throw new MovementBadRequest("Esta operacion solo permite en movimientos de tipo TRANSFERENCIA");
        }
    }
}
