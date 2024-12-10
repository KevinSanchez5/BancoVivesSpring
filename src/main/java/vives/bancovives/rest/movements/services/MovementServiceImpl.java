package vives.bancovives.rest.movements.services;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que implementa la interfaz MovementService y que se encarga de gestionar los movimientos
 */
@Service
@Slf4j
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

    /**
     * Método que se encarga de buscar todos los movimientos en la base de datos
     * @param movementType Tipo de movimiento
     * @param ibanOfReference Iban de la cuenta de referencia
     * @param fecha Fecha del movimiento
     * @param clientOfReferenceDni Dni del cliente de referencia
     * @param clientOfDestinationDni Dni del cliente de destino
     * @param isDeleted Si el movimiento ha sido eliminado
     * @param pageable Paginación
     * @return Page<MovementResponseDto> Lista de movimientos
     */
    @Override
    public Page<MovementResponseDto> findAll(
            Optional<String> movementType,
            Optional<String> ibanOfReference,
            Optional<String> fecha,
            Optional<String> clientOfReferenceDni,
            Optional<String> clientOfDestinationDni,
            Optional<Boolean> isDeleted,
            Pageable pageable) {

        Optional<LocalDate> parsedFecha = fecha.map(f->{
            try{
                return LocalDate.parse(f);
            }catch (DateTimeParseException e){
                throw new MovementBadRequest("Formato de fecha invalido, Debe ser con formato: aaaa-mm-dd");
            }
        });
        Page<Movement> movements = movementRepository.findAllByFilters(movementType, ibanOfReference, parsedFecha, clientOfReferenceDni, clientOfDestinationDni, isDeleted, pageable);
        return movements.map(movementMapper::fromEntityToResponse);
    }

    /**
     * Método que se encarga de buscar un movimiento por su id
     * @param id Id del movimiento
     * @return MovementResponseDto Movimiento
     */
    @Override
    public MovementResponseDto findById(ObjectId id) {
        return movementMapper.fromEntityToResponse(existsMovementById(id));
    }

    /**
     * Método que se encarga de guardar un movimiento en la base de datos
     * @param principal Usuario autenticado
     * @param movementCreateDto Movimiento a guardar
     * @return  MovementResponseDto Movimiento guardado
     */
    @Transactional
    @Override
    public MovementResponseDto save(Principal principal, MovementCreateDto movementCreateDto) {
        validator.validateMovementDto(movementCreateDto);
        validateUser(principal, movementCreateDto.getIbanOfReference());

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

    /**
     * Método que se encarga de añadir interes a un movimiento
     * @param createDto
     * @return MovementResponseDto Movimiento con interes añadido
     */
    @Transactional
    @Override
    public MovementResponseDto addInterest(MovementCreateDto createDto){
        if(!createDto.getMovementType().trim().equalsIgnoreCase("INTERESMENSUAL")){
            throw new MovementBadRequest("No se puede añadir interes a un movimiento que no sea de tipo interes mensual");
        }
        Account accountOfReference = existsAccountByIban(createDto.getIbanOfReference());
        validator.validateInteresMensual(createDto, accountOfReference);
        Movement movement = movementMapper.fromCreateDtoToEntity(createDto, accountOfReference, null, null);
        movement.setAmountOfMoney(calculateInterest(accountOfReference));
        moveMoney(movement);
        saveModificationsInAccountsAndCard(accountOfReference, null, null);
        return movementMapper.fromEntityToResponse(movementRepository.save(movement));
    }

    /**
     * Método que se encarga de actualizar un movimiento
     * @param id Id del movimiento
     * @param movementDto Movimiento a actualizar
     * @return MovementResponseDto Movimiento actualizado
     */
    @Transactional
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

    /**
     * Método que se encarga de eliminar un movimiento
     * @param id Id del movimiento
     * @return Void
     */
    @Override
    public Void deleteById(ObjectId id) {
        Movement movementToDelete = existsMovementById(id);
        movementToDelete.setIsDeleted(true);
        movementRepository.save(movementToDelete);
        return null;
    }

    /**
     * Método que se encarga de cancelar un movimiento
     * @param principal Usuario autenticado
     * @param id Id del movimiento
     * @return Boolean
     */
    @Transactional
    @Override
    public Boolean cancelMovement(Principal principal, ObjectId id) {
        Movement movementToCancel = existsMovementById(id);

        verifyIsATransferencia(movementToCancel.getMovementType());
        validateUser(principal, movementToCancel.getAccountOfReference().getIban());

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(movementToCancel.getCreatedAt(), now);

        if (duration.toHours() >= 24) {
            throw new MovementBadRequest("El movimiento no puede cancelarse porque han pasado más de 24 horas.");
        }

        revertTransfer(movementToCancel.getAccountOfReference(), movementToCancel.getAccountOfDestination(), movementToCancel.getAmountOfMoney());

        movementRepository.delete(movementToCancel);

        return true;
    }

    /**
     * Método que se encarga de buscar un movimiento por su id
     * @param id Id del movimiento
     * @return
     */
    public Movement existsMovementById(ObjectId id){
        return movementRepository.findById(id).orElseThrow(
                () -> new MovementNotFound("Movimiento con id" + id.toHexString() + "no encontrado"));
    }

    /**
     * Método que se encarga de buscar una cuenta por su iban
     * @param iban Iban de la cuenta
     * @return Account Cuenta
     * @throws AccountNotFoundException Excepción si la cuenta no existe
     */
    public Account existsAccountByIban(String iban){
        return accountRepository.findByIban(iban).orElseThrow(
                () -> new AccountNotFoundException("Cuenta con iban" + iban + "no encontrada"));
    }

    /**
     * Método que se encarga de buscar una tarjeta por su número
     * @param cardNumber
     * @return
     */
    public Card existsCardByCardNumber(String cardNumber){
        return cardsRepository.findByCardNumber(cardNumber).orElseThrow(
                () -> new CardDoesNotExistException("Tarjeta con numero" + cardNumber + "no encontrada"));
    }


    /**
     * Se encarga de mover el dinero dependiendo del tipo de transaccion e insertar el movimiento en la base de datos
     *
     * @param movement Movimiento a realizar
     */
    public void moveMoney(Movement movement){
        switch (movement.getMovementType()){
            case TRANSFERENCIA:
                movement.setAmountBeforeMovement(movement.getAccountOfReference().getBalance());
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() - movement.getAmountOfMoney());
                movement.getAccountOfDestination().setBalance(movement.getAccountOfDestination().getBalance() + movement.getAmountOfMoney());
                movement.setClientOfDestinationDni(movement.getAccountOfDestination().getClient().getDni());
                movement.setClientOfReferenceDni(movement.getAccountOfReference().getClient().getDni());
                break;
            case INGRESO, NOMINA:
                movement.setClientOfDestinationDni(movement.getAccountOfReference().getClient().getDni());
                movement.setAmountBeforeMovement(movement.getAccountOfReference().getBalance());
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() + movement.getAmountOfMoney());
                break;
            case PAGO:
            case EXTRACCION:
                movement.setAmountBeforeMovement(movement.getAccountOfReference().getBalance());
                movement.setClientOfReferenceDni(movement.getAccountOfReference().getClient().getDni());
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() - movement.getAmountOfMoney());
                setNewLimitsInCard(movement.getCard(), movement.getAmountOfMoney());
                break;
            case INTERESMENSUAL:
                movement.setAmountBeforeMovement(movement.getAccountOfReference().getBalance());
                movement.setClientOfReferenceDni(movement.getAccountOfReference().getClient().getDni());
                movement.getAccountOfReference().setBalance(movement.getAccountOfReference().getBalance() + calculateInterest(movement.getAccountOfReference()));
        }
    }

    /**
     * Calcula el interes que se va a realizar en una cuenta de un movimiento de tipo interes mensual
     * @param accountOfReference
     * @return
     */
    private Double calculateInterest(Account accountOfReference){
        return accountOfReference.getBalance() * (accountOfReference.getAccountType().getInterest()/100);
    }

    /**
     * Actualiza los limites de una tarjeta despues de realizar un movimiento de tipo PAGO o EXTRACCION
     * @param card
     * @param amount
     */
    public void setNewLimitsInCard(Card card, Double amount){
        card.setSpentToday(card.getSpentToday() + amount);
        card.setSpentThisWeek(card.getSpentThisWeek() + amount);
        card.setSpentThisMonth(card.getSpentThisMonth() + amount);
    }

    /**
     * Guarda las modificaciones en las cuentas y tarjetas despues de realizar un movimiento
     * @param accountOfReference
     * @param accountOfDestination
     * @param card
     */
    private void saveModificationsInAccountsAndCard(Account accountOfReference, Account accountOfDestination, Card card){
        accountRepository.save(accountOfReference);
        if(accountOfDestination != null){
            accountRepository.save(accountOfDestination);
        }
        if(card != null){
            cardsRepository.save(card);
        }
    }

    /**
     * Revierte una transferencia en caso de que se cancele
     * @param accountOfReference
     * @param accountOfDestination
     * @param amount
     */
    private void revertTransfer(Account accountOfReference, Account accountOfDestination, Double amount){
        accountOfReference.setBalance(accountOfReference.getBalance() + amount);
        accountOfDestination.setBalance(accountOfDestination.getBalance() - amount);
        accountRepository.save(accountOfReference);
        accountRepository.save(accountOfDestination);
    }

    /**
     * Verifica si el movimiento es de tipo TRANSFERENCIA
     * @param movemntType
     */
    private void verifyIsATransferencia(MovementType movemntType){
        if(movemntType != MovementType.TRANSFERENCIA){
            throw new MovementBadRequest("Esta operacion solo permite en movimientos de tipo TRANSFERENCIA");
        }
    }


    /**
     * Este método se ejecuta a las 00 de la noche todos los dias de manera automatica, para que busque las cuentas que
     * tengan interes, si se cumple el mes desde que se creo la cuenta, se le añade el interes mensual a la cuenta
     * y se almacena el movimiento y el cambio en el balance de la cuenta en las bases de datos
     */
    @Scheduled(cron = "0 0 0 * * ?")// se ejecutaria a las 00
    @Transactional
    public void createInteresMensualMovement(){
        List<Account> accountsWithInterest = accountRepository.findAllByAccountType_InterestNotNull();
        for (Account account : accountsWithInterest) {
            LocalDate creationDate = account.getCreatedAt().toLocalDate();
            LocalDate today = LocalDate.now();

            if (creationDate.getDayOfMonth() == today.getDayOfMonth() ||
                    (creationDate.getDayOfMonth() > today.lengthOfMonth() && today.getDayOfMonth() == today.lengthOfMonth())) {
                Movement movement = Movement.builder()
                        .movementType(MovementType.INTERESMENSUAL)
                        .accountOfReference(account)
                        .amountOfMoney(calculateInterest(account))
                        .build();

                moveMoney(movement);
                saveModificationsInAccountsAndCard(account, null, null);
            }
        }
    }

    /**
     * Método que se encarga de buscar los movimientos de un usuario
     * @param principal Usuario autenticado
     * @param pageable Paginación
     * @return Page<MovementResponseDto> Lista de movimientos
     */
    @Override
    public Page<MovementResponseDto> findMyMovements(Principal principal, Pageable pageable){
        log.info("Buscando sus movientos");

        List<Account> accounts = accountRepository.findAllByClient_User_Username(principal.getName());
        if(accounts.isEmpty()){
            throw new MovementNotFound("No se han encontrado cuentas para el usuario con username " + principal.getName());
        }

        List<Movement> movements = accounts.stream()
                .flatMap(account -> movementRepository.findAllByAccountOfReference_Iban(account.getIban()).stream())
                .toList();

        if(movements.isEmpty()){
            throw new MovementNotFound("No se han encontrado movimientos para las cuentas del usuario con username " + principal.getName());
        }
        List<MovementResponseDto> responses = movements.stream().map(movementMapper::fromEntityToResponse).toList();

        return getPage(responses, pageable);
    }

    /**
     * Método que se encarga de paginar una lista de movimientos
     * @param list Lista de movimientos
     * @param pageable Paginación
     * @param <T> Tipo de movimiento
     * @return Page<T> Lista de movimientos paginada
     */
    private <T> Page<T> getPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start >= list.size()) {
            return Page.empty(pageable);
        }

        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    /**
     * Método que se encarga de validar si un usuario tiene acceso a una cuenta
     * @param principal Usuario autenticado
     * @param ibanOfReference Iban de la cuenta
     */
    public void validateUser(Principal principal, String ibanOfReference){
        Authentication authentication = (Authentication) principal;
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isAdmin) {
            List<Account> accountsOfUser = accountRepository.findAllByClient_User_Username(principal.getName());

            if (accountsOfUser.isEmpty()) {
                throw new AccountNotFoundException("No se han encontrado cuentas para el usuario con username " + principal.getName());
            }

            boolean ibanBelongsToUser = accountsOfUser.stream().anyMatch(account -> account.getIban().equals(ibanOfReference));

            if (!ibanBelongsToUser) {
                throw new AccountNotFoundException("La cuenta con iban " + ibanOfReference + " no pertenece al usuario con username " + principal.getName());
            }
        }
    }

}

