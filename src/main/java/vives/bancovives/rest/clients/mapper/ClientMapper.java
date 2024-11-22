package vives.bancovives.rest.clients.mapper;

import org.springframework.stereotype.Component;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.model.Address;
import vives.bancovives.rest.clients.model.Client;

import java.time.LocalDateTime;

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
        newClient.setAddress(new Address(
                createDto.getStreet().trim(),
                createDto.getHouseNumber().trim(),
                createDto.getCity().trim().toUpperCase(),
                createDto.getCountry().trim().toUpperCase()
        ));
        newClient.setValidated(false);
        return newClient;
    }

    public Client fromUpdateDtoToEntity(Client client, ClientUpdateDto updateDto) {
        Address updatedAddress = new Address(
                updateDto.getStreet() != null ? updateDto.getStreet().trim() : client.getAddress().getStreet(),
                updateDto.getHouseNumber() != null ? updateDto.getHouseNumber().trim() : client.getAddress().getHouseNumber(),
                updateDto.getCity() != null ? updateDto.getCity().trim().toUpperCase() : client.getAddress().getCity(),
                updateDto.getCountry() != null ? updateDto.getCountry().trim().toUpperCase() : client.getAddress().getCountry()
        );
        return new Client (
                client.getId(),
                client.getPublicId(),
                updateDto.getDni() != null ? updateDto.getDni().toUpperCase() : client.getDni(),
                updateDto.getCompleteName() != null ? updateDto.getCompleteName().trim() : client.getCompleteName(),
                updatedAddress,
                updateDto.getEmail() != null ? updateDto.getEmail().trim() : client.getEmail(),
                updateDto.getPhoneNumber() != null ? updateDto.getPhoneNumber() : client.getPhoneNumber(),
                updateDto.getPhoto() != null ? updateDto.getPhoto() : client.getPhoto(),
                updateDto.getDniPicture() != null ? updateDto.getDniPicture() : client.getDniPicture(),
                false,
                false,
                client.getCreatedAt(),
                LocalDateTime.now()
        );
    }

    public ClientResponseDto fromEntityToResponse(Client client){
        return new ClientResponseDto(
                client.getPublicId(),
                client.getDni(),
                client.getCompleteName(),
                client.getEmail(),
                client.getPhoneNumber(),
                client.getPhoto(),
                client.getDniPicture(),
                client.getAddress(),
                client.isValidated(),
                client.isDeleted(),
                client.getCreatedAt().toString(),
                client.getUpdatedAt().toString()
        );
    }
}
