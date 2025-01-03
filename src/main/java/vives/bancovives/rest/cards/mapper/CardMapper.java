package vives.bancovives.rest.cards.mapper;

import vives.bancovives.rest.accounts.mapper.AccountMapper;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.dto.output.OutputCard;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.products.cardtype.mappers.CardTypeMapper;
import vives.bancovives.rest.products.cardtype.model.CardType;

import java.time.LocalDateTime;

/**
 * Esta clase proporciona métodos para mapear objetos relacionados con {@link Card}.
 */
public class CardMapper {

    /**
     * Mapea un objeto {@link InputCard} a un objeto {@link Card}.
     *
     * @param inputCard El objeto {@link InputCard} que se va a mapear.
     * @param cardType  El tipo de tarjeta asociado.
     * @param account   La cuenta asociada.
     * @return Un objeto {@link Card} que contiene los datos mapeados.
     */
    public static Card toCard(InputCard inputCard, CardType cardType, Account account) {
        return Card.builder()
                .cardOwner(inputCard.getCardOwner().trim().toUpperCase())
                .pin(inputCard.getPin())
                .cardType(cardType)
                .account(account)
                .dailyLimit(inputCard.getDailyLimit())
                .weeklyLimit(inputCard.getWeeklyLimit())
                .monthlyLimit(inputCard.getMonthlyLimit())
                .build();
    }

    /**
     * Mapea un objeto {@link UpdateRequestCard} a un objeto {@link Card} existente.
     *
     * @param request El objeto {@link UpdateRequestCard} que se va a mapear.
     * @param card    El objeto {@link Card} existente que se va a actualizar.
     * @return Un objeto {@link Card} que contiene los datos actualizados.
     */
    public static Card toCard(UpdateRequestCard request, Card card) {
        return Card.builder()
                .id(card.getId())
                .publicId(card.getPublicId())
                .cardOwner(card.getCardOwner())
                .pin(request.getPin() != null ? request.getPin() : card.getPin())
                .dailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() : card.getDailyLimit())
                .weeklyLimit(request.getWeeklyLimit() != null ? request.getWeeklyLimit() : card.getWeeklyLimit())
                .monthlyLimit(request.getMonthlyLimit() != null ? request.getMonthlyLimit() : card.getMonthlyLimit())
                .cvv(card.getCvv())
                .cardType(card.getCardType())
                .cardNumber(card.getCardNumber())
                .isInactive(request.getIsInactive() != null ? request.getIsInactive() : card.getIsInactive())
                .isDeleted(card.getIsDeleted())
                .expirationDate(card.getExpirationDate())
                .creationDate(card.getCreationDate())
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    /**
     * Mapea un objeto {@link Card} a un objeto {@link OutputCard}.
     *
     * @param card El objeto {@link Card} que se va a mapear.
     * @return Un objeto {@link OutputCard} que contiene los datos mapeados.
     */
    public static OutputCard toOutputCard(Card card) {
        return OutputCard.builder()
                .id(card.getPublicId())
                .cardNumber(card.getCardNumber())
                .cardOwner(card.getCardOwner())
                .expirationDate(String.valueOf(card.getExpirationDate()))
                .cvv(card.getCvv())
                .pin(card.getPin())
                .cardType(CardTypeMapper.toOutputCardType(card.getCardType()))
                .account(AccountMapper.toOutputAccount(card.getAccount()))
                .dailyLimit(card.getDailyLimit())
                .weeklyLimit(card.getWeeklyLimit())
                .monthlyLimit(card.getMonthlyLimit())
                .isInactive(card.getIsInactive())
                .isDeleted(card.getIsDeleted())
                .creationDate(String.valueOf(card.getCreationDate()))
                .lastUpdate(String.valueOf(card.getLastUpdate()))
                .build();
    }
}