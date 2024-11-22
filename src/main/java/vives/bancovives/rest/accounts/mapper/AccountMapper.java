package vives.bancovives.rest.accounts.mapper;

import vives.bancovives.rest.accounts.dto.input.InputAccount;
import vives.bancovives.rest.accounts.dto.output.OutputAccount;
import vives.bancovives.rest.accounts.model.Account;

public class AccountMapper {

    public static OutputAccount toOutputAccount(Account account) {
        return OutputAccount.builder()
            .id(account.getId())
            .iban(account.getIban())
            .balance(account.getBalance())
            .createdAt(account.getCreatedAt().toString())
            .updatedAt(account.getUpdatedAt().toString())
            .isDeleted(account.isDeleted())
            .build();
    }

    public static Account toAccount(InputAccount inputAccount) {
        return Account.builder()
            .balance(inputAccount.getBalance())
            .password(inputAccount.getPassword())
            .build();
    }
}
