package vives.bancovives.rest.movements.services;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.cards.repository.CardsRepository;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.movements.dtos.input.MovementCreateDto;
import vives.bancovives.rest.movements.dtos.output.MovementResponseDto;
import vives.bancovives.rest.movements.exceptions.MovementBadRequest;
import vives.bancovives.rest.movements.exceptions.MovementNotFound;
import vives.bancovives.rest.movements.mapper.MovementMapper;
import vives.bancovives.rest.movements.model.Movement;
import vives.bancovives.rest.movements.model.MovementType;
import vives.bancovives.rest.movements.repository.MovementRepository;
import vives.bancovives.rest.movements.validator.MovementValidator;
import vives.bancovives.rest.products.accounttype.model.AccountType;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MovementServiceImplTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardsRepository cardsRepository;

    @Mock
    private MovementValidator validator;

    @Mock
    private MovementMapper movementMapper;

    @InjectMocks
    private MovementServiceImpl movementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll_Success() {
        // Given
        Page<Movement> mockPage = mock(Page.class);
        MovementResponseDto responseDto = new MovementResponseDto();
        when(movementRepository.findAllByFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(mockPage);
        when(mockPage.map(any())).thenReturn(mock(Page.class));

        // When
        Page<MovementResponseDto> result = movementService.findAll(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Pageable.unpaged()
        );

        // Then
        assertNotNull(result);
        verify(movementRepository).findAllByFilters(any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void testFindById_Success() {
        // Given
        ObjectId id = new ObjectId();
        Movement movement = new Movement();
        when(movementRepository.findById(id)).thenReturn(Optional.of(movement));
        when(movementMapper.fromEntityToResponse(movement)).thenReturn(new MovementResponseDto());

        // When
        MovementResponseDto result = movementService.findById(id);

        // Then
        assertNotNull(result);
        verify(movementRepository).findById(id);
    }

    @Test
    void testAddInterest_Success() {
        // Given
        MovementCreateDto createDto = new MovementCreateDto();
        createDto.setMovementType("INTERESMENSUAL");
        createDto.setIbanOfReference("ES1234567890");

        AccountType accountType = AccountType.builder().interest(0.01).build();
        Client client = Client.builder().dni("123").build();
        Account account = Account.builder().iban("123").client(client).accountType(accountType).balance(1000).build();
        Movement movement = Movement.builder().movementType(MovementType.INTERESMENSUAL).accountOfReference(account).build();
        MovementResponseDto responseDto = new MovementResponseDto();

        when(accountRepository.findByIban(anyString())).thenReturn(Optional.of(account));
        when(movementMapper.fromCreateDtoToEntity(any(), any(), any(), any())).thenReturn(movement);
        when(movementRepository.save(movement)).thenReturn(movement);
        when(movementMapper.fromEntityToResponse(movement)).thenReturn(responseDto);

        // When
        MovementResponseDto result = movementService.addInterest(createDto);

        // Then
        assertNotNull(result);
        verify(movementRepository).save(movement);
    }

    @Test
    void testAddInterest_ThrowsExceptionForInvalidType() {
        // Given
        MovementCreateDto createDto = new MovementCreateDto();
        createDto.setMovementType("TRANSFERENCIA");

        // When & Then
        assertThrows(MovementBadRequest.class, () -> movementService.addInterest(createDto));
    }

    @Test
    void testValidateUser_Success() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");

        // Mocking the authorities to simulate a non-admin user
        GrantedAuthority authority = mock(GrantedAuthority.class);
        when(authority.getAuthority()).thenReturn("ROLE_USER");

        // Mock the account repository to return a user account
        Account account = new Account();
        account.setIban("ES1234567890");
        when(accountRepository.findAllByClient_User_Username("user@example.com")).thenReturn(Collections.singletonList(account));

        // When
        movementService.validateUser(authentication, "ES1234567890");

        // Then
        // No exception should be thrown, test passes
    }

    @Test
    void testCancelMovement_Success() {
        // Given
        ObjectId id = new ObjectId("5f50c31d6f703e2230d0478c");
        Movement movement = new Movement();
        movement.setId(id);
        movement.setMovementType(MovementType.TRANSFERENCIA);
        movement.setAccountOfReference(Account.builder().iban("ES1234567890").build());
        movement.setAccountOfDestination(Account.builder().iban("ES0987654321").build());

        movement.setAmountOfMoney(100.0);
        movement.setCreatedAt(LocalDateTime.now().minusHours(1)); // Movimiento creado hace 1 hora

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");

        when(movementRepository.findById(id)).thenReturn(Optional.of(movement));
        when(accountRepository.findAllByClient_User_Username(anyString())).thenReturn(Collections.singletonList(movement.getAccountOfReference()));

        // When
        Boolean result = movementService.cancelMovement(authentication, id);

        // Then
        assertTrue(result); // Verificamos que la cancelación fue exitosa
        verify(movementRepository, times(1)).delete(movement); // Verificamos que se llamó a delete
    }

    @Test
    void testCancelMovement_FailedDueToTimeLimit() {
        // Given
        ObjectId id = new ObjectId("5f50c31d6f703e2230d0478c");
        Movement movement = new Movement();
        movement.setId(id);
        movement.setMovementType(MovementType.TRANSFERENCIA);
        movement.setAccountOfReference(Account.builder().iban("ES1234567890").balance(0).build());
        movement.setAccountOfDestination(Account.builder().iban("ES0987654321").build());
        movement.setAmountOfMoney(100.0);
        movement.setCreatedAt(LocalDateTime.now().minusDays(2)); // Movimiento creado hace más de 24 horas

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");

        when(movementRepository.findById(id)).thenReturn(Optional.of(movement));
        when(accountRepository.findAllByClient_User_Username(anyString())).thenReturn(Collections.singletonList(movement.getAccountOfReference()));

        // When & Then
        MovementBadRequest exception = assertThrows(MovementBadRequest.class, () -> {
            movementService.cancelMovement(authentication, id);
        });
        assertEquals("El movimiento no puede cancelarse porque han pasado más de 24 horas.", exception.getMessage());
    }
    @Test
    void testExistsMovementById_Found() {
        // Given
        ObjectId id = new ObjectId("5f50c31d6f703e2230d0478c");
        Movement movement = new Movement();
        movement.setId(id);

        when(movementRepository.findById(id)).thenReturn(Optional.of(movement));

        // When
        Movement result = movementService.existsMovementById(id);

        // Then
        assertNotNull(result); // Verificamos que el movimiento no es nulo
    }

    @Test
    void testExistsMovementById_NotFound() {
        // Given
        ObjectId id = new ObjectId("5f50c31d6f703e2230d0478c");

        when(movementRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        MovementNotFound exception = assertThrows(MovementNotFound.class, () -> {
            movementService.existsMovementById(id);
        });
        assertEquals("Movimiento con id5f50c31d6f703e2230d0478cno encontrado", exception.getMessage());
    }


    @Test
    void testMoveMoney_Transferencia() {
        // Given
        Movement movement = new Movement();
        Client client =  Client.builder().dni("123").build();
        Client client2 = Client.builder().dni("456").build();
        movement.setMovementType(MovementType.TRANSFERENCIA);
        movement.setAmountOfMoney(100.0);
        movement.setAccountOfReference(Account.builder().iban("ES1234567890").client(client).balance(500).build());
        movement.setAccountOfDestination(Account.builder().iban("ES0987654321").client(client2).balance(200).build());
        // When
        movementService.moveMoney(movement);

        // Then
        assertEquals(400.0, movement.getAccountOfReference().getBalance()); // Balance de referencia después de la transferencia
        assertEquals(300.0, movement.getAccountOfDestination().getBalance()); // Balance de destino después de la transferencia
    }

    @Test
    void testMoveMoney_Ingreso() {
        // Given
        Movement movement = new Movement();
        Client client =  Client.builder().dni("123").build();
        movement.setMovementType(MovementType.INGRESO);
        movement.setAmountOfMoney(100.0);
        movement.setAccountOfReference(Account.builder().client(client).iban("ES1234567890").balance(500.0).build());

        // When
        movementService.moveMoney(movement);

        // Then
        assertEquals(600.0, movement.getAccountOfReference().getBalance()); // Balance después del ingreso
    }

    @Test
    void testSetNewLimitsInCard() {
        // Given
        Card card = new Card();
        card.setSpentToday(50.0);
        card.setSpentThisMonth(200.0);

        Movement movement = new Movement();
        movement.setCard(card);
        movement.setAmountOfMoney(30.0);

        // When
        movementService.setNewLimitsInCard(card, movement.getAmountOfMoney());

        // Then
        assertEquals(80.0, card.getSpentToday()); // Nuevo gasto del día
        assertEquals(230.0, card.getSpentThisMonth()); // Nuevo gasto del mes
    }

}
