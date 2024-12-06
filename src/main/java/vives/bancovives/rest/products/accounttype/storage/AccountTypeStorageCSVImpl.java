package vives.bancovives.rest.products.accounttype.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;
import vives.bancovives.rest.products.exceptions.ProductStorageException;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Esta clase implementa la interfaz AccountTypeStorageCSV, proporcionando métodos para guardar y leer tipos de cuentas en un archivo CSV.
 *
 * @author Diego Novillo Luceño
 * @since 1.0
 */
@Service
@Slf4j
public class AccountTypeStorageCSVImpl implements AccountTypeStorageCSV {

    /**
     * Guarda una lista de tipos de cuentas en un archivo CSV.
     *
     * @param data La lista de tipos de cuentas que se van a guardar.
     * @param file El archivo en el que se guardarán los tipos de cuentas.
     * @throws ProductStorageException Si hay un error al guardar los tipos de cuentas en el archivo.
     */
    @Override
    public void save(List<AccountType> data, File file) {
        log.info("Exportando tipos de cuentas a un fichero CSV");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("nombre,interés,descripción\n");
            data.forEach(accountType -> {
                try {
                    writer.write(accountType.getName() + "," + accountType.getInterest() + "," + accountType.getDescription() + "\n");
                } catch (IOException e) {
                    throw new ProductStorageException("Hubo un error al guardar los tipos de cuenta en un fichero CSV:" + e);
                }
            });
        } catch (IOException e) {
            throw new ProductStorageException("Hubo un error al guardar los tipos de cuenta en un fichero CSV:" + e);
        }
    }

    /**
     * Lee tipos de cuentas desde un archivo CSV y los devuelve como un Flux.
     *
     * @param file El archivo desde el que se leerán los tipos de cuentas.
     * @return Un Flux de objetos {@link NewAccountType} que representan los tipos de cuentas leídos del archivo.
     * @throws ProductStorageException Si hay un error al leer los tipos de cuentas del archivo.
     */
    @Override
    public Flux<NewAccountType> read(File file) {
        log.info("Importando tipos de cuentas a un fichero CSV");
        return Flux.create(fluxSink -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Read the first line to check if it's a header
                String firstLine = reader.readLine();
                if (firstLine == null) {
                    fluxSink.complete();
                    return;
                }
                Stream<String> lines;
                if (Objects.equals(firstLine, "nombre,interés,descripción")) {
                    lines = reader.lines();
                } else {
                    lines = Stream.concat(Stream.of(firstLine), reader.lines());
                }

                lines.forEach(line -> {
                            String[] info = line.split(",");
                            NewAccountType newAccountType = NewAccountType.builder()
                                    .name(info[0])
                                    .interest(Double.parseDouble(info[1]))
                                    .description(info[2])
                                    .build();
                            fluxSink.next(newAccountType);
                        });
                fluxSink.complete();
            } catch (IOException e) {
                fluxSink.error(e);
            }
        });
    }

}
