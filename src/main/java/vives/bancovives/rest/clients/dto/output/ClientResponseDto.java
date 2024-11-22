package vives.bancovives.rest.clients.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.clients.model.Adress;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponseDto {

    private UUID id;
    private String idPath;
    private String dni;
    private String completeName;
    private String email;
    private String phoneNumber;
    private String photo;
    private String dniPicture;
    private Adress adress;
    private Boolean validated;
    private String createdAt;
    private String updatedAt;
}
