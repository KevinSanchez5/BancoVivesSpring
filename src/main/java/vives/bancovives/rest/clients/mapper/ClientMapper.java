package vives.bancovives.rest.clients.mapper;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.accounts.dto.output.AccountResponseForClient;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.model.Address;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.users.dto.output.UserResponse;
import vives.bancovives.rest.users.models.Role;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.utils.IdGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Component
public class ClientMapper {

    public Client fromCreateDtoToEntity(ClientCreateDto createDto) {
        Client newClient = new Client(
                createDto.getDni().toUpperCase(),
                createDto.getCompleteName().trim(),
                createDto.getEmail().trim(),
                createDto.getPhoneNumber(),
                createDto.getPhoto(),
                createDto.getDniPicture()
        );
        User newUser = new User(
                UUID.randomUUID(),
                IdGenerator.generateId(),
                createDto.getUsername(),
                createDto.getPassword(),
                Collections.singleton(Role.USER),
                newClient,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );
        newClient.setAddress(new Address(
                createDto.getStreet().trim(),
                createDto.getHouseNumber().trim(),
                createDto.getCity().trim().toUpperCase(),
                createDto.getCountry().trim().toUpperCase()
        ));
        newClient.setValidated(false);
        newClient.setUser(newUser);
        return newClient;
    }

    public Client fromUpdateDtoToEntity(Client client, ClientUpdateDto updateDto) {
        Address updatedAddress = new Address(
                updateDto.getStreet() != null ? updateDto.getStreet().trim() : client.getAddress().getStreet(),
                updateDto.getHouseNumber() != null ? updateDto.getHouseNumber().trim() : client.getAddress().getHouseNumber(),
                updateDto.getCity() != null ? updateDto.getCity().trim().toUpperCase() : client.getAddress().getCity(),
                updateDto.getCountry() != null ? updateDto.getCountry().trim().toUpperCase() : client.getAddress().getCountry()
        );
        Client updatedClient = new Client (
                client.getId(),
                client.getPublicId(),
                updateDto.getDni() != null ? updateDto.getDni().toUpperCase() : client.getDni(),
                updateDto.getCompleteName() != null ? updateDto.getCompleteName().trim() : client.getCompleteName(),
                updatedAddress,
                updateDto.getEmail() != null ? updateDto.getEmail().trim() : client.getEmail(),
                updateDto.getPhoneNumber() != null ? updateDto.getPhoneNumber() : client.getPhoneNumber(),
                updateDto.getPhoto() != null ? updateDto.getPhoto() : client.getPhoto(),
                updateDto.getDniPicture() != null ? updateDto.getDniPicture() : client.getDniPicture(),
                client.getUser(),
                client.getAccounts(),
                false,
                false,
                client.getCreatedAt(),
                LocalDateTime.now()
        );
        if(updateDto.getUsername() != null || updateDto.getPassword() != null) {
            updatedClient.getUser().setUsername(updateDto.getUsername() != null ? updateDto.getUsername().trim() : client.getUser().getUsername());
            updatedClient.getUser().setPassword(updateDto.getPassword() != null ? updateDto.getPassword().trim() : client.getUser().getPassword());
            updatedClient.getUser().setUpdatedAt(LocalDateTime.now());
        }
        return updatedClient;
    }

    public ClientResponseDto fromEntityToResponse(Client client){
        UserResponse userResponse = null;
        if(client.getUser()!=null){
            userResponse = new UserResponse(
                client.getUser().getPublicId(),
                client.getUser().getUsername(),
                client.getUser().getRoles(),
                client.getUser().getIsDeleted()
        );}
        List<AccountResponseForClient> accounts = client.getAccounts().stream().map(account -> new AccountResponseForClient(account.getPublicId(), account.getIban(), account.getBalance())).collect(toList());
        return new ClientResponseDto(
                client.getPublicId(),
                client.getDni(),
                client.getCompleteName(),
                client.getEmail(),
                client.getPhoneNumber(),
                client.getPhoto(),
                client.getDniPicture(),
                client.getAddress(),
                userResponse,
                accounts,
                client.isValidated(),
                client.isDeleted(),
                client.getCreatedAt().toString(),
                client.getUpdatedAt().toString()
        );
    }
}
