package vives.bancovives.rest.users.repositories;

import vives.bancovives.rest.users.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameEqualsIgnoreCase(String username);

    Optional<User> findByPublicId(String publicId);

    @Modifying // Para indicar que es una consulta de actualización
    @Query("UPDATE User u SET u.isDeleted = true WHERE u.publicId = :publicId")
        // Consulta de actualización
    void updateIsDeletedToTrueByPublicId(String publicId);
}
