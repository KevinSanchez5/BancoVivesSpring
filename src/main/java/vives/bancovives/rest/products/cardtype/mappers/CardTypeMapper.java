package vives.bancovives.rest.products.cardtype.mappers;

import vives.bancovives.rest.products.cardtype.dto.input.NewCardType;
import vives.bancovives.rest.products.cardtype.dto.input.UpdatedCardType;
import vives.bancovives.rest.products.cardtype.dto.output.OutputCardType;
import vives.bancovives.rest.products.cardtype.model.CardType;

import javax.smartcardio.Card;
import java.time.LocalDateTime;

/**
 * Esta clase proporciona métodos para mapear objetos que tienen que ver con {@link CardType}
 */
public class CardTypeMapper {
    // Un contructor privado para evitar la creación de esta clase
    private CardTypeMapper() {}

    /**
     * Mapea un objeto {@link CardType} a un objeto {@link OutputCardType}.
     *
     * @param input El objeto {@link CardType} que se va a mapear.
     * @return Un objeto {@link OutputCardType} que contiene los datos mapeados.
     */
    public static OutputCardType toOutputCardType(CardType input) {
        return OutputCardType.builder()
                .id(input.getPublicId())
                .name(input.getName())
                .description(input.getDescription())
                .createdAt(input.getCreatedAt().toString())
                .updatedAt(input.getUpdatedAt().toString())
                .isDeleted(input.getIsDeleted())
                .build();
    }

    /**
     * Mapea un objeto {@link OutputCardType} a un objeto {@link CardType}.
     *
     * @param newCardType El objeto {@link NewCardType} que se va a mapear.
     * @return Un objeto {@link CardType} que contiene los datos mapeados.
     */
    public static CardType toCardType(NewCardType newCardType) {
        return CardType.builder()
                .name(newCardType.getName().trim().toUpperCase())
                .description(newCardType.getDescription())
                .build();
    }

    /**
     * Actualiza los campos de un objeto {@link CardType} existente con los valores de un objeto {@link UpdatedCardType}.
     *
     * @param existingCardType el objeto {@link CardType} que se va a actualizar
     * @param updatedCardType  el objeto {@link UpdatedCardType} que contiene los nuevos valores
     */
    public static void updateCardTypeFromInput(CardType existingCardType, UpdatedCardType updatedCardType) {
        if (updatedCardType.getName() != null){
            existingCardType.setName(updatedCardType.getName().trim().toUpperCase());
        }
        if (updatedCardType.getDescription() != null){
            existingCardType.setDescription(updatedCardType.getDescription());
        }
        existingCardType.setUpdatedAt(LocalDateTime.now());
    }
}
