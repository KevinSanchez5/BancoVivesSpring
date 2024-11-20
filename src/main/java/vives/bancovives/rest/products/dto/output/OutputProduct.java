package vives.bancovives.rest.products.dto.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class OutputProduct {
    private UUID id;
    private String name;
    private String productType;
    private String description;
    private Double interest;
    private String createdAt;
    private String updatedAt;
    private Boolean isDeleted;
}
