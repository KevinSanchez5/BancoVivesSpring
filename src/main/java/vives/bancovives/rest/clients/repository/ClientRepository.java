package vives.bancovives.rest.clients.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vives.bancovives.rest.clients.model.Client;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de la entidad Client.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {
    Optional<Client> findByDniIgnoreCase(String dni);
    Optional<Client> findByEmailIgnoreCase(String email);
    Optional<Client> findByPublicId(String publicId);
    Optional<Client> findByUser_Username(String username);
}
