package vives.bancovives.rest.accounts.mapper;

import vives.bancovives.rest.accounts.dto.input.InputAccount;
import vives.bancovives.rest.accounts.dto.output.OutputAccount;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseForAccount;
import vives.bancovives.rest.clients.mapper.ClientMapper;
import vives.bancovives.rest.clients.model.Client;

public class AccountMapper {



    public static OutputAccount toOutputAccount(Account account) {
        ClientResponseForAccount clientResponseForAccount = new ClientResponseForAccount(
                account.getClient().getPublicId(),
                account.getClient().getDni(),
                account.getClient().getCompleteName(),
                account.getClient().getEmail()
                );
        return OutputAccount.builder()
            .id(account.getId())
            .iban(account.getIban())
            .balance(account.getBalance())
            .client(clientResponseForAccount)
            .createdAt(account.getCreatedAt().toString())
            .updatedAt(account.getUpdatedAt().toString())
            .isDeleted(account.isDeleted())
            .build();
    }

    public static Account toAccount(InputAccount inputAccount, Client client) {
        return Account.builder()
            .balance(inputAccount.getBalance())
            .password(inputAccount.getPassword())
            .client(client)
            .build();
    }
}
