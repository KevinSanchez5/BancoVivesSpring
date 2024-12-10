package vives.bancovives.rest.clients.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.accounts.dto.output.AccountResponseSimplified;
import vives.bancovives.rest.clients.model.Address;
import vives.bancovives.rest.users.dto.output.UserResponse;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponseDto {

    private String publicId;
    private String dni;
    private String completeName;
    private String email;
    private String phoneNumber;
    private String photo;
    private String dniPicture;
    private Address address;
    private UserResponse userResponse;
    private List<AccountResponseSimplified> accounts;
    private Boolean validated;
    private Boolean isDeleted;
    private String createdAt;
    private String updatedAt;
}
