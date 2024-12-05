package vives.bancovives.rest.products.accounttype.mappers;

import vives.bancovives.rest.products.accounttype.dto.input.NewAccountType;
import vives.bancovives.rest.products.accounttype.dto.input.UpdatedAccountType;
import vives.bancovives.rest.products.accounttype.dto.output.OutputAccountType;
import vives.bancovives.rest.products.accounttype.model.AccountType;

import java.time.LocalDateTime;

/**
 * Esta clase proporciona métodos para mapear objetos que tienen que ver con {@link AccountType}
 */
public class AccountTypeMapper {
    // Un contructor privado para evitar la creación de esta clase
    private AccountTypeMapper() {}

    /**
     * Mapea un objeto {@link AccountType} a un objeto {@link OutputAccountType}.
     *
     * @param input El objeto {@link AccountType} que se va a mapear.
     * @return Un objeto {@link OutputAccountType} que contiene los datos mapeados.
     */
    public static OutputAccountType toOutputAccountType(AccountType input) {
        return OutputAccountType.builder()
                .id(input.getPublicId())
                .name(input.getName())
                .description(input.getDescription())
                .interest(input.getInterest())
                .createdAt(input.getCreatedAt().toString())
                .updatedAt(input.getUpdatedAt().toString())
                .isDeleted(input.getIsDeleted())
                .build();
    }

    /**
     * Mapea un objeto {@link NewAccountType} a un objeto {@link AccountType}.
     *
     * @param newAccountType El objeto {@link NewAccountType} que se va a mapear.
     * @return Un objeto {@link AccountType} que contiene los datos mapeados.
     */
    public static AccountType toAccountType(NewAccountType newAccountType) {
        return AccountType.builder()
                .name(newAccountType.getName().trim().toUpperCase())
                .description(newAccountType.getDescription())
                .interest(newAccountType.getInterest())
                .build();
    }

    /**
     * Actualiza los campos de un objeto {@link AccountType} existente con los valores de un objeto {@link UpdatedAccountType}.
     *
     * @param existingAccountType el objeto {@link AccountType} que se va a actualizar
     * @param updatedAccountType  el objeto {@link UpdatedAccountType} que contiene los nuevos valores
     */
    public static void updateAccountTypeFromInput(AccountType existingAccountType, UpdatedAccountType updatedAccountType) {
        if (updatedAccountType.getName() != null){
            existingAccountType.setName(updatedAccountType.getName().trim().toUpperCase());
        }
        if (updatedAccountType.getInterest() != null){
            existingAccountType.setInterest(updatedAccountType.getInterest());
        }
        if (updatedAccountType.getDescription()!= null){
            existingAccountType.setDescription(updatedAccountType.getDescription());
        }
        existingAccountType.setUpdatedAt(LocalDateTime.now());
    }
}
