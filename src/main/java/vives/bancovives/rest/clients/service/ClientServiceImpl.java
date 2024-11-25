package vives.bancovives.rest.clients.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.exceptions.ClientConflict;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.mapper.ClientMapper;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.repository.ClientRepository;
import vives.bancovives.rest.clients.validators.ClientUpdateValidator;
import vives.bancovives.rest.users.services.UsersService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"clients"})
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UsersService userService;
    private final ClientMapper clientMapper;
    private final ClientUpdateValidator updateValidator;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, UsersService userService, ClientMapper clientMapper, ClientUpdateValidator updateValidator) {
        this.clientRepository = clientRepository;
        this.userService = userService;
        this.clientMapper = clientMapper;
        this.updateValidator = updateValidator;
    }

    @Override
    public Page<ClientResponseDto> findAll(
            Optional<String> dni,
            Optional<String> completeName,
            Optional<String> email,
            Optional<String> street,
            Optional<String> city,
            Optional<Boolean> validated,
            Optional<Boolean> isDeleted,
            Pageable pageable) {
        log.info("Buscando clientes");
        Specification<Client> specDni = (root, query, criteriaBuilder) ->
                dni.map(dn -> criteriaBuilder.like(criteriaBuilder.upper(root.get("dni")), "%" + dn.toUpperCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Client> specCompleteName = (root, query, criteriaBuilder) ->
                completeName.map(cn -> criteriaBuilder.like(criteriaBuilder.upper(root.get("completeName")), "%" + cn.toUpperCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Client> specEmail = (root, query, criteriaBuilder) ->
                email.map(em -> criteriaBuilder.like(criteriaBuilder.upper(root.get("email")), "%" + em.toUpperCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Client> specStreet = (root, query, criteriaBuilder) ->
                street.map(st -> criteriaBuilder.like(criteriaBuilder.upper(root.get("adress").get("street")), "%" + st.toUpperCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Client> specCity = (root, query, criteriaBuilder) ->
                city.map(ci -> criteriaBuilder.like(criteriaBuilder.upper(root.get("adress").get("city")), "%" + ci.toUpperCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Client> specValidated = (root, query, criteriaBuilder) ->
                validated.map(v -> criteriaBuilder.equal(root.get("validated"), v))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Client> specIsDeleted = (root, query, criteriaBuilder) ->
                isDeleted.map(d -> criteriaBuilder.equal(root.get("isDeleted"), d))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));

        Specification<Client> criterio = Specification.where(specDni).and(specCompleteName).and(specEmail).and(specStreet).and(specCity).and(specValidated).and(specIsDeleted);
        return clientRepository.findAll(criterio, pageable).map(clientMapper::fromEntityToResponse);
    }

    @Override
    public ClientResponseDto findById(String id) {
        log.info("Buscando el cliente con id: " + id);
        return clientMapper.fromEntityToResponse(existClientByPublicId(id));
    }

    @Override
    public ClientResponseDto save(ClientCreateDto createDto) {
        log.info("Guardando un nuevo cliente");
        Client clientToSave = clientMapper.fromCreateDtoToEntity(createDto);
        existsClientByDniAndEmail(createDto.getDni(), createDto.getEmail());
        userService.saveUserFromClient(clientToSave.getUser());
        return clientMapper.fromEntityToResponse(clientRepository.save(clientToSave));
    }

    @Override
    public ClientResponseDto update(String id, ClientUpdateDto updateDto) {
        log.info("Actualizando cliente con id: " + id);
        updateValidator.validateUpdateDto(updateDto);
        existsClientByDniAndEmail(updateDto.getDni(), updateDto.getEmail());
        Client client = existClientByPublicId(id);
        Client updatedClient = clientMapper.fromUpdateDtoToEntity(client, updateDto);
        if(updateDto.getUsername() != null || updateDto.getPassword() != null) {
            userService.updateUserFromClient(client.getUser().getPublicId(), updatedClient.getUser());
        }
        return clientMapper.fromEntityToResponse(clientRepository.save(updatedClient));
    }

    @Override
    public ClientResponseDto deleteByIdLogically(String id, Optional<Boolean> deleteData) {
        log.info("Borrando cliente con id: " + id);
        Client client = existClientByPublicId(id);
        if (deleteData.isPresent() && deleteData.get()) {
            userService.deleteById(client.getUser().getPublicId());
            return deleteDataOfClient(client);
        }
        userService.deleteById(client.getUser().getPublicId());
        client.setDeleted(true);
        return clientMapper.fromEntityToResponse(clientRepository.save(client));
    }

    @Override
    public ClientResponseDto deleteDataOfClient(Client client) {
        log.info("Borrando datos del cliente con id: " + client.getId());
        client.setDni(null);
        client.setCompleteName(null);
        client.setEmail(null);
        client.setPhoneNumber(null);
        client.setPhoto(null);
        client.setDniPicture(null);
        client.setAddress(null);
        client.setUpdatedAt(LocalDateTime.now());
        client.setDeleted(true);
        return clientMapper.fromEntityToResponse(clientRepository.save(client));
    }

    public void existsClientByDniAndEmail(String dni, String email) {
        if (clientRepository.findByDniIgnoreCase(dni).isPresent()) {
            throw new ClientConflict("Cliente con ese dni ya existe");
        }
        if (clientRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ClientConflict("Ese email ya esta en uso");
        }
    }

    public ClientResponseDto validateClient(String id) {
        Client client = existClientByPublicId(id);
        client.setValidated(true);
        return clientMapper.fromEntityToResponse(clientRepository.save(client));
    }

    public Client existClientByPublicId(String id) {
        return clientRepository.findByPublicId(id).orElseThrow(
                () -> new ClientNotFound("El cliente con id: " + id + " no encontrado"));
    }
}

