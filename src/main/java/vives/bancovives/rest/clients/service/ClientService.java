package vives.bancovives.rest.clients.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.model.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientService {

    Page<Client> findAll(
            Optional<String> dni,
            Optional<String> completeName,
            Optional<String> email,
            Optional<String> street,
            Optional<String> city,
            Optional<Boolean> validated,
            Optional<Boolean> isDeleted,
            Pageable pageable
    );

    Client findById(UUID id);
    Client save(ClientCreateDto createDto);
    Client update(UUID id, ClientUpdateDto updateDto);
    Client deleteByIdLogically(UUID id, Optional<Boolean> deleteData);
    Client deleteDataOfClient(UUID id);
    Client validateClient(UUID id);

}
