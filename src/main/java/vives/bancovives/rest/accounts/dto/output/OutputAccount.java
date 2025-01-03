package vives.bancovives.rest.accounts.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vives.bancovives.rest.clients.dto.output.ClientResponseSimplified;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputAccount {
    private String id;
    private String iban;
    private double balance;
    private String accountType;
    private ClientResponseSimplified client;
    private String createdAt;
    private String updatedAt;
    private boolean isDeleted;
}