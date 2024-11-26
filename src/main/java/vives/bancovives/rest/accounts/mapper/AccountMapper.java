package vives.bancovives.rest.accounts.mapper;

import vives.bancovives.rest.accounts.dto.input.InputAccount;
import vives.bancovives.rest.accounts.dto.output.OutputAccount;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.clients.dto.output.ClientResponseForAccount;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.products.accounttype.model.AccountType;

public class AccountMapper {

    public static OutputAccount toOutputAccount(Account account) {
        ClientResponseForAccount clientResponseForAccount = new ClientResponseForAccount(
                account.getClient().getPublicId(),
                account.getClient().getDni(),
                account.getClient().getCompleteName(),
                account.getClient().getEmail()
                );
        return OutputAccount.builder()
                .id(account.getPublicId())
                .iban(account.getIban())
                .balance(account.getBalance())
                .client(clientResponseForAccount)
                .createdAt(account.getCreatedAt().toString())
                .updatedAt(account.getUpdatedAt().toString())
                .isDeleted(account.isDeleted())
                .accountType(account.getAccountType().getName())
                .build();
    }

    public static Account toAccount(InputAccount inputAccount, AccountType accountType, Client client) {
        return Account.builder()
                .password(inputAccount.getPassword())
                .accountType(accountType)
                .client(client)
                .build();
    }
}
