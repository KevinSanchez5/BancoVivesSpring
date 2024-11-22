package vives.bancovives.rest.clients.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.exceptions.ClientConflict;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.mapper.ClientMapper;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.repository.ClientRepository;
import vives.bancovives.rest.clients.validators.ClientUpdateValidator;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@CacheConfig(cacheNames = {"clients"})
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final ClientUpdateValidator updateValidator;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, ClientMapper clientMapper, ClientUpdateValidator updateValidator) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.updateValidator = updateValidator;
    }

    @Override
    public Page<Client> findAll(
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
        return clientRepository.findAll(criterio, pageable);
    }

    @Override
    @Cacheable(key = "#id")
    public Client findById(UUID id) {
        log.info("Buscando el cliente con id: " + id);
        return clientRepository.findById(id).orElseThrow(
                () -> new ClientNotFound("El cliente con id: " + id + " no encontrado"));
    }

    @Override
    @CachePut(key = "#result.id")
    public Client save(ClientCreateDto createDto) {
        log.info("Guardando un nuevo cliente");
        Client clientToSave = clientMapper.fromCreateDtoToEntity(createDto);
        existsClient(createDto.getDni(), createDto.getEmail());
        return clientRepository.save(clientToSave);
    }

    @Override
    @CachePut(key = "#result.id")
    public Client update(UUID id, ClientUpdateDto updateDto) {
        log.info("Actualizando cliente con id: " + id);
        updateValidator.validateUpdateDto(updateDto);
        existsClient(updateDto.getDni(), updateDto.getEmail());
        Client client = findById(id);
        Client updatedClient = clientMapper.fromUpdateDtoToEntity(client, updateDto);
        return clientRepository.save(updatedClient);
    }

    @Override
    @CacheEvict(key = "#id")
    public Client deleteByIdLogically(UUID id, Optional<Boolean> deleteData) {
        log.info("Borrando cliente con id: " + id);
        Client client = findById(id);
        if (deleteData.isPresent() && deleteData.get()) {
            return deleteDataOfClient(id);
        }
        client.setDeleted(true);
        return clientRepository.save(client);
    }

    @Override
    public Client deleteDataOfClient(UUID id) {
        log.info("Borrando datos del cliente con id: " + id);
        Client client = findById(id);
        client.setDni(null);
        client.setCompleteName(null);
        client.setEmail(null);
        client.setPhoneNumber(null);
        client.setPhoto(null);
        client.setDniPicture(null);
        client.setAdress(null);
        client.setUpdatedAt(LocalDateTime.now());
        client.setDeleted(true);
        return clientRepository.save(client);
    }

    public void existsClient(String dni, String email) {
        if (clientRepository.findByDniIgnoreCase(dni).isPresent()) {
            throw new ClientConflict("Cliente con ese dni ya existe");
        }
        if (clientRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ClientConflict("Ese email ya esta en uso");
        }

    }

    public Client validateClient(UUID id) {
        Client client = clientRepository.findById(id).orElseThrow(
                () -> new ClientNotFound("El cliente con id: " + id + " no encontrado"));
        client.setValidated(true);
        return clientRepository.save(client);
    }
}

