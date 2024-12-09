package vives.bancovives.rest.clients.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.accounts.service.AccountService;
import vives.bancovives.rest.clients.dto.input.ClientCreateDto;
import vives.bancovives.rest.clients.dto.input.ClientUpdateDto;
import vives.bancovives.rest.clients.dto.output.ClientResponseDto;
import vives.bancovives.rest.clients.exceptions.ClientBadRequest;
import vives.bancovives.rest.clients.exceptions.ClientConflict;
import vives.bancovives.rest.clients.exceptions.ClientNotFound;
import vives.bancovives.rest.clients.mapper.ClientMapper;
import vives.bancovives.rest.clients.model.Client;
import vives.bancovives.rest.clients.repository.ClientRepository;
import vives.bancovives.rest.clients.validators.ClientUpdateValidator;
import vives.bancovives.rest.users.models.User;
import vives.bancovives.rest.users.services.UsersService;
import vives.bancovives.storage.exceptions.StorageException;
import vives.bancovives.storage.service.StorageService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@Service
@CacheConfig(cacheNames = {"clients"})
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final AccountService accountService;
    private final StorageService storageService;
    private final UsersService userService;
    private final ClientMapper clientMapper;
    private final ClientUpdateValidator updateValidator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, UsersService userService, AccountService accountService, StorageService storageService, ClientMapper clientMapper, ClientUpdateValidator updateValidator, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.userService = userService;
        this.accountService = accountService;
        this.storageService = storageService;
        this.clientMapper = clientMapper;
        this.updateValidator = updateValidator;
        this.passwordEncoder = passwordEncoder;
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
                street.map(st -> criteriaBuilder.like(criteriaBuilder.upper(root.get("address").get("street")), "%" + st.toUpperCase() + "%"))
                        .orElseGet(() -> criteriaBuilder.isTrue(criteriaBuilder.literal(true)));
        Specification<Client> specCity = (root, query, criteriaBuilder) ->
                city.map(ci -> criteriaBuilder.like(criteriaBuilder.upper(root.get("address").get("city")), "%" + ci.toUpperCase() + "%"))
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
        User user = clientToSave.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User userToSave = userService.saveUserFromClient(user);
        clientToSave.setUser(userToSave);
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
            User userUpdated = userService.updateUserFromClient(client.getUser().getPublicId(), updatedClient.getUser());
            updatedClient.setUser(userUpdated);
        }
        return clientMapper.fromEntityToResponse(clientRepository.save(updatedClient));
    }

    @Override
    public ClientResponseDto deleteByIdLogically(String id, Optional<Boolean> deleteData) {
        log.info("Borrando cliente con id: " + id);
        Client client = existClientByPublicId(id);
        if(client.getUser()!=null){
            String userPublicId = client.getUser().getPublicId();
            client.setUser(null);
            userService.deleteById(userPublicId);
        }
        if(!client.getAccounts().isEmpty()) {
            for(Account account : client.getAccounts()) {
                accountService.deleteById(account.getPublicId());
            }
        }
        if (deleteData.isPresent() && deleteData.get()) {
            return deleteDataOfClient(client);
        }
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
        client.setValidated(false);
        client.setUpdatedAt(LocalDateTime.now());
        client.setDeleted(true);
        return clientMapper.fromEntityToResponse(clientRepository.save(client));
    }

    public ClientResponseDto findMe(Principal principal) {
        String username = principal.getName();
        Client client = clientRepository.findByUser_Username(username).orElseThrow(
                ()-> new ClientNotFound("Cliente no encontrado")
        );
        return clientMapper.fromEntityToResponse(client);
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
        if(client.getDniPicture()==null){
            throw new ClientBadRequest("No se puede validar un cliente sin imagen de dni");
        }
        client.setValidated(true);
        return clientMapper.fromEntityToResponse(clientRepository.save(client));
    }

    public Client existClientByPublicId(String id) {
        return clientRepository.findByPublicId(id).orElseThrow(
                () -> new ClientNotFound("El cliente con id: " + id + " no encontrado"));
    }

    @Override
    public Map<String, Object> storeImage(String id, MultipartFile file, String campo){
        String urlImagen = null;
        Client client = existClientByPublicId(id);
        if (!file.isEmpty()) {
            if(campo.equals("photo") && client.getPhoto()!=null){
                storageService.delete(client.getPhoto());
            } else if(campo.equals("dniPicture") && client.getDniPicture()!=null){
                storageService.delete(client.getDniPicture());
            }
            String imagen = storageService.store(file);
            urlImagen = storageService.getUrl(imagen);
            if(campo.equals("photo")){
                client.setPhoto(urlImagen);
            } else {
                client.setDniPicture(urlImagen);
            }
            clientRepository.save(client);
            return Map.of("url", urlImagen);
        } else {
            throw new StorageException("No se puede subir un fichero vacío");
        }
    }
}

