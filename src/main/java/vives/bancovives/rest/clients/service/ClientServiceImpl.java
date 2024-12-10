package vives.bancovives.rest.clients.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación de la interfaz ClientService.
 * Proporciona métodos para buscar, guardar, actualizar y eliminar clientes.
 * Además, proporciona métodos para validar un cliente, buscar un cliente por su id y exportar los datos de un cliente como JSON.
 * También proporciona métodos para subir una imagen de un cliente y para borrar los datos de un cliente.
 *
 * @author Kelvin Jesús Sánchez Barahona
 * @since 1.0.0
 */
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
    private final ObjectMapper jsonMapper;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, UsersService userService, AccountService accountService, StorageService storageService, ClientMapper clientMapper, ClientUpdateValidator updateValidator, PasswordEncoder passwordEncoder, ObjectMapper jsonMapper) {
        this.clientRepository = clientRepository;
        this.userService = userService;
        this.accountService = accountService;
        this.storageService = storageService;
        this.clientMapper = clientMapper;
        this.updateValidator = updateValidator;
        this.passwordEncoder = passwordEncoder;
        this.jsonMapper = jsonMapper;
        jsonMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Método que busca clientes filtrados por diferentes criterios
     * @param dni dni de un cliente
     * @param completeName nombre completo de un cliente
     * @param email email de un cliente
     * @param street calle de un cliente
     * @param city ciudad de un cliente
     * @param validated si el cliente esta validado
     * @param isDeleted si el cliente esta borrado
     * @param pageable paginación
     * @return Página de clientes que cumplen los criterios de búsqueda
     */
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

    /**
     * Método que busca un cliente por su id
     * @param id id del cliente
     * @return Cliente con el id especificado
     */
    @Override
    public ClientResponseDto findById(String id) {
        log.info("Buscando el cliente con id: " + id);
        return clientMapper.fromEntityToResponse(existClientByPublicId(id));
    }

    /**
     * Método que guarda un nuevo cliente y su usuario asociado
     * @param createDto datos del cliente a guardar
     * @return ClienteResponseDto el cliente guardado mapeado a un dto de respuesta
     */
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

    /**
     * Método que actualiza un cliente, tambien actualiza el usuario asociado si se proporciona un nuevo username o password
     * @param id id del cliente a actualizar
     * @param updateDto datos del cliente a actualizar
     * @return ClienteResponseDto el cliente actualizado mapeado a un dto de respuesta
     */
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

    /**
     * Método que borra un cliente de forma lógica, si se proporciona deleteData como true, se borran todos los datos del cliente
     * tambien borra el usuario asociado y todas las cuentas asociadas al cliente de manera logica
     * @param id id del cliente a borrar
     * @param deleteData si se deben borrar los datos del cliente
     * @return ClienteResponseDto el cliente borrado mapeado a un dto de respuesta
     */
    @Override
    public ClientResponseDto deleteByIdLogically(String id, Optional<Boolean> deleteData) {
        log.info("Borrando cliente con id: " + id);
        Client client = existClientByPublicId(id);
        if(client.getUser()!=null){
            String userPublicId = client.getUser().getPublicId();
            client.setUser(null);
            userService.deleteById(userPublicId);
        }
        if(client.getPhoto()!=null){
            storageService.delete(client.getPhoto());
        }
        if(client.getDniPicture()!=null){
            storageService.delete(client.getDniPicture());
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

    /**
     * Método que borra todos los datos de un cliente
     * @param client cliente al que se le van a borrar los datos
     * @return ClienteResponseDto el cliente con los datos borrados mapeado a un dto de respuesta
     */
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

    /**
     * Método que busca el cliente asociado al usuario que ha hecho la petición
     * @param principal usuario que ha hecho la petición
     * @return ClienteResponseDto el cliente asociado al usuario que ha hecho la petición mapeado a un dto de respuesta
     */
    public ClientResponseDto findMe(Principal principal) {
        Client client = findClientByPrincipal(principal);
        return clientMapper.fromEntityToResponse(client);
    }

    /**
     * Método que busca el cliente asociado al usuario que ha hecho la petición
     * @param principal usuario que ha hecho la petición
     * @return Cliente asociado al usuario que ha hecho la petición
     */
    public Client findClientByPrincipal(Principal principal) {
        String username = principal.getName();
        return clientRepository.findByUser_Username(username).orElseThrow(
                ()-> new ClientNotFound("Cliente no encontrado")
        );
    }

    /**
     * Método que valida que no exista un cliente con el mismo dni o email
     * @param dni dni del cliente
     * @param email email del cliente
     */
    public void existsClientByDniAndEmail(String dni, String email) {
        if (clientRepository.findByDniIgnoreCase(dni).isPresent()) {
            throw new ClientConflict("Cliente con ese dni ya existe");
        }
        if (clientRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ClientConflict("Ese email ya esta en uso");
        }
    }

    /**
     * Método que valida un cliente, para validar un cliente se necesita una imagen de su dni
     * @param id id del cliente a validar
     * @return ClienteResponseDto el cliente validado mapeado a un dto de respuesta
     */
    public ClientResponseDto validateClient(String id) {
        Client client = existClientByPublicId(id);
        if(client.getDniPicture()==null){
            throw new ClientBadRequest("No se puede validar un cliente sin imagen de dni");
        }
        client.setValidated(true);
        return clientMapper.fromEntityToResponse(clientRepository.save(client));
    }

    /**
     * Método que busca un cliente por su id
     * @param id id del cliente
     * @return Cliente con el id especificado
     */
    public Client existClientByPublicId(String id) {
        return clientRepository.findByPublicId(id).orElseThrow(
                () -> new ClientNotFound("El cliente con id: " + id + " no encontrado"));
    }

    /**
     * Método que sube una imagen de un cliente y la guarda en el sistema de almacenamiento
     * @param principal usuario que ha hecho la petición
     * @param file imagen a subir
     * @param campo campo de la imagen a subir
     * @return Map con la url de la imagen subida
     */
    @Override
    public Map<String, Object> storeImage(Principal principal, MultipartFile file, String campo){
        String urlImagen = null;
        Client client = findClientByPrincipal(principal);
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

    /**
     * Método que exporta los datos de un cliente como JSON
     * @param principal usuario que ha hecho la petición
     * @return Resource con los datos del cliente exportados como JSON
     */
    @Override
    public Resource exportMeAsJson(Principal principal) {
        log.info("Exportando datos del cliente con id: " + principal.getName());
        Client client = findClientByPrincipal(principal);
        try {
            File tempFile = File.createTempFile("client", ".json");
            jsonMapper.writeValue(tempFile, client);
            return new FileSystemResource(tempFile);
        } catch (IOException e) {
            log.error("Error al convertir el cliente a JSON");
            throw new StorageException("Error al exportar el cliente como JSON", e);
        }
    }
}

