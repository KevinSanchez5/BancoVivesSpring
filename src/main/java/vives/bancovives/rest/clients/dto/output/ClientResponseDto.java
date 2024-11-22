package vives.bancovives.rest.clients.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.clients.model.Address;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponseDto {

    private String idPath;
    private String dni;
    private String completeName;
    private String email;
    private String phoneNumber;
    private String photo;
    private String dniPicture;
    private Address address;
    private Boolean validated;
    private Boolean isDeleted;
    private String createdAt;
    private String updatedAt;
}
