package vives.bancovives.rest.products.accounttype.storage;

import reactor.core.publisher.Flux;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;

import java.io.File;
import java.util.List;

public interface AccountTypeStorageCSV {
    void save(List<AccountType> data, File file);
    Flux<NewAccountType> read(File file);
}
