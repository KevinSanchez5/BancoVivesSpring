package vives.bancovives.rest.accounts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import vives.bancovives.rest.accounts.dto.input.InputAccount;
import vives.bancovives.rest.accounts.exception.AccountNotFoundException;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.repositories.AccountRepository;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.repository.ClientRepository;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.accounttype.repositories.AccountTypeRepository;
import vives.bancovives.utils.IdGenerator;
import vives.bancovives.utils.account.IbanGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountTypeRepository accountTypeRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;
    private InputAccount inputAccount;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .publicId(IdGenerator.generateId())
                .iban(IbanGenerator.generateIban("ES"))
                .client(Client.builder().dni("12345678A").build())
                .accountType(AccountType.builder().name("SAVINGS").build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        inputAccount = InputAccount.builder()
                .dni("12345678A")
                .accountType("SAVINGS")
               // Ensure the IBAN is set
                .build();
    }

    @Test
    void findByIdReturnsAccount() {
        when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.of(account));
        Account result = accountService.findById(account.getPublicId());
        assertEquals(account, result);
    }

    @Test
    void findByIdThrowsAccountNotFoundException() {
        when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.findById(account.getPublicId()));
    }

    @Test
    void findByIbanReturnsAccount() {
        when(accountRepository.findByIban(account.getIban())).thenReturn(Optional.of(account));
        Account result = accountService.findByIban(account.getIban());
        assertEquals(account, result);
    }

    @Test
    void findByIbanThrowsAccountNotFoundException() {
        when(accountRepository.findByIban(account.getIban())).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.findByIban(account.getIban()));
    }

    // @Test
    // void saveCreatesNewAccount() {
    //     when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(account.getClient()));
    //      when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
    //      when(accountRepository.save(any(Account.class))).thenReturn(account);
    //    Account result = accountService.save(inputAccount);
    //      assertEquals(account, result);
    // }

    //  @Test
    //  void saveThrowsAccountConflictException() {
        //      when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(account.getClient()));
        //      when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
        //      when(accountRepository.findByIban(account.getIban())).thenReturn(Optional.of(account));
        //      assertThrows(AccountConflictException.class, () -> accountService.save(inputAccount));
        //  }

    @Test
    void deleteByIdMarksAccountAsDeleted() {
        when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        Account result = accountService.deleteById(account.getPublicId());
        assertTrue(result.isDeleted());
    }

    @Test
    void deleteByIdThrowsAccountNotFoundException() {
        when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.deleteById(account.getPublicId()));
    }

   // @Test
   // void updateByIdUpdatesAccount() {
   //  when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.of(account));
   //  when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(account.getClient()));
   //  when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
   //   when(accountRepository.save(any(Account.class))).thenReturn(account);
   //   Account result = accountService.updateById(account.getPublicId(), inputAccount);
    //     assertEquals(account, result);
    // }

    @Test
    void updateByIdThrowsAccountNotFoundException() {
        when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.updateById(account.getPublicId(), inputAccount));
    }

    @Test
    void findAllReturnsAccounts() {
        Page<Account> accountPage = new PageImpl<>(List.of(account));
        when(accountRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(accountPage);
        Page<Account> result = accountService.findAll(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    //  @Test
    //  void saveThrowsClientBadRequestWhenClientNotValidated() {
        //   Client invalidClient = Client.builder().dni("12345678A").validated(false).build();
        //   when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(invalidClient));
        //   when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
        //   assertThrows(ClientBadRequest.class, () -> accountService.save(inputAccount));
        // }

    //   @Test
    //  void updateByIdThrowsClientBadRequestWhenClientNotValidated() {
    //     Client invalidClient = Client.builder().dni("12345678A").validated(false).build();
    //     when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.of(account));
    //    when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(invalidClient));
    //     when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
    //     assertThrows(ClientBadRequest.class, () -> accountService.updateById(account.getPublicId(), inputAccount));
    //  }

    //   @Test
    //  void saveThrowsClientNotFoundWhenClientDeleted() {
    //      Client deletedClient = Client.builder().dni("12345678A").isDeleted(true).build();
    //      when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(deletedClient));
    //     when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
    //      assertThrows(ClientNotFound.class, () -> accountService.save(inputAccount));
    //  }

    // @Test
    //   void updateByIdThrowsClientNotFoundWhenClientDeleted() {
    //     Client deletedClient = Client.builder().dni("12345678A").isDeleted(true).build();
    //  when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.of(account));
    //     when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(deletedClient));
    //    when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
    //     assertThrows(ClientNotFound.class, () -> accountService.updateById(account.getPublicId(), inputAccount));
    //  }

    //   @Test
    //   void saveThrowsProductDoesNotExistExceptionWhenAccountTypeNotFound() {
      //      when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(account.getClient()));
    //    when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.empty());
    //  assertThrows(ProductDoesNotExistException.class, () -> accountService.save(inputAccount));
    //}

    //@Test
    //void updateByIdThrowsProductDoesNotExistExceptionWhenAccountTypeNotFound() {
    //   when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.of(account));
    //  when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(account.getClient()));
    //  when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.empty());
    //  assertThrows(ProductDoesNotExistException.class, () ->
    //          accountService.updateById(account.getPublicId(), inputAccount)
    //  );

        // Validamos que los métodos fueron invocados correctamente
        //  verify(accountRepository).findByPublicId(account.getPublicId());
        //verify(clientRepository).findByDniIgnoreCase("12345678A");
    //verify(accountTypeRepository).findByName("SAVINGS");
    //}


    //@Test
    //void saveThrowsAccountConflictExceptionWhenIbanExists() {
        // when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(account.getClient()));
        //when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
        //when(accountRepository.findByIban(account.getIban())).thenReturn(Optional.of(account));
        //assertThrows(AccountConflictException.class, () -> accountService.save(inputAccount));
        //}

    //@Test
    //  void updateByIdThrowsClientBadRequestWhenDniMismatch() {
        //Client differentClient = Client.builder().dni("87654321B").validated(true).build();
        //when(accountRepository.findByPublicId(account.getPublicId())).thenReturn(Optional.of(account));
        //   when(clientRepository.findByDniIgnoreCase("12345678A")).thenReturn(Optional.of(differentClient));
        //when(accountTypeRepository.findByName("SAVINGS")).thenReturn(Optional.of(account.getAccountType()));
        //assertThrows(ClientBadRequest.class, () -> accountService.updateById(account.getPublicId(), inputAccount));
        //}
}