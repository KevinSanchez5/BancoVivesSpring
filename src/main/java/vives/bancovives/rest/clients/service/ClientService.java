package vives.bancovives.rest.clients.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.model.Client;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

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

    ClientResponseDto findById(String id);
    ClientResponseDto save(ClientCreateDto createDto);
    ClientResponseDto update(String id, ClientUpdateDto updateDto);
    ClientResponseDto deleteByIdLogically(String id, Optional<Boolean> deleteData);
    ClientResponseDto deleteDataOfClient(Client client);
    ClientResponseDto validateClient(String id);
    ClientResponseDto findMe(Principal principal);
    Map<String, Object> storeImage(Principal principal, MultipartFile file, String campo);
    Resource exportMeAsJson(Principal principal);
}
