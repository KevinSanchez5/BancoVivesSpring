package vives.bancovives.rest.products.cardtype.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class OutputCardType {
    private String id;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    private Boolean isDeleted;
}
