package vives.bancovives.rest.accounts.mapper;

import org.junit.jupiter.api.Test;
import vives.bancovives.rest.accounts.dto.input.InputAccount;
import vives.bancovives.rest.accounts.dto.output.OutputAccount;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.products.accounttype.model.AccountType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperTest {

    @Test
    void toOutputAccountMapsCorrectly() {
        Client client = Client.builder()
                .publicId("client-public-id")
                .dni("12345678A")
                .completeName("John Doe")
                .email("john.doe@example.com")
                .build();

        AccountType accountType = AccountType.builder()
                .name("SAVINGS")
                .build();

        Account account = Account.builder()
                .publicId("account-public-id")
                .iban("ES1234567890")
                .balance(1000.0)
                .client(client)
                .createdAt(LocalDateTime.of(2022, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2022, 1, 2, 12, 0))
                .isDeleted(false)
                .accountType(accountType)
                .build();

        OutputAccount outputAccount = AccountMapper.toOutputAccount(account);

        assertEquals("account-public-id", outputAccount.getId());
        assertEquals("ES1234567890", outputAccount.getIban());
        assertEquals(1000.0, outputAccount.getBalance());
        assertEquals("client-public-id", outputAccount.getClient().getPublicId());
        assertEquals("12345678A", outputAccount.getClient().getDni());
        assertEquals("John Doe", outputAccount.getClient().getCompleteName());
        assertEquals("john.doe@example.com", outputAccount.getClient().getEmail());
        assertEquals("2022-01-01T12:00", outputAccount.getCreatedAt());
        assertEquals("2022-01-02T12:00", outputAccount.getUpdatedAt());
        assertFalse(outputAccount.isDeleted());
        assertEquals("SAVINGS", outputAccount.getAccountType());
    }

    @Test
    void toAccountMapsCorrectly() {
        InputAccount inputAccount = InputAccount.builder()
                .password("password")
                .accountType("SAVINGS")
                .dni("12345678A")
                .build();

        Client client = Client.builder()
                .publicId("client-public-id")
                .dni("12345678A")
                .build();

        AccountType accountType = AccountType.builder()
                .name("SAVINGS")
                .build();

        Account account = AccountMapper.toAccount(inputAccount, accountType, client);

        assertEquals("password", account.getPassword());
        assertEquals(accountType, account.getAccountType());
        assertEquals(client, account.getClient());
    }

    @Test
    void toAccountHandlesNullValues() {
        InputAccount inputAccount = InputAccount.builder()
                .password(null)
                .accountType(null)
                .dni(null)
                .build();

        Client client = Client.builder()
                .publicId("client-public-id")
                .dni("12345678A")
                .build();

        AccountType accountType = AccountType.builder()
                .name("SAVINGS")
                .build();

        Account account = AccountMapper.toAccount(inputAccount, accountType, client);

        assertNull(account.getPassword());
        assertEquals(accountType, account.getAccountType());
        assertEquals(client, account.getClient());
    }
}