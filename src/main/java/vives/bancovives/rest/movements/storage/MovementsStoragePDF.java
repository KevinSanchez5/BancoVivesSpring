package vives.bancovives.rest.movements.storage;

import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.movements.model.Movement;

import java.io.File;
import java.util.List;

public interface MovementsStoragePDF {
    void save(File file, Account account, List<Movement> movements);
}
