package vives.bancovives.rest.clients.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.model.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientService {

    Page<ClientResponseDto> findAll(
            Optional<String> dni,
            Optional<String> completeName,
            Optional<String> email,
            Optional<String> street,
            Optional<String> city,
            Optional<Boolean> validated,
            Optional<Boolean> isDeleted,
            Pageable pageable
    );

    ClientResponseDto findById(UUID id);
    ClientResponseDto save(ClientCreateDto createDto);
    ClientResponseDto update(UUID id, ClientUpdateDto updateDto);
    ClientResponseDto deleteByIdLogically(UUID id, Optional<Boolean> deleteData);
    ClientResponseDto deleteDataOfClient(Client client);
    ClientResponseDto validateClient(UUID id);

}
