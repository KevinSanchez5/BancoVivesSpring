package vives.bancovives.rest.cards.mapper;

import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.dto.output.OutputCard;
import vives.bancovives.rest.cards.model.Card;
import vives.bancovives.rest.products.cardtype.mappers.CardTypeMapper;
import vives.bancovives.rest.products.cardtype.model.CardType;

import java.time.LocalDateTime;

public class CardMapper {

    public static Card toCard(InputCard inputCard, CardType cardType) {
        return Card.builder()
                .cardOwner(inputCard.getCardOwner().trim().toUpperCase())
                .pin(inputCard.getPin())
                .cardType(cardType)
                .dailyLimit(inputCard.getDailyLimit())
                .weeklyLimit(inputCard.getWeeklyLimit())
                .monthlyLimit(inputCard.getMonthlyLimit())
                .build();
    }

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
                .isInactive((request.getIsInactive() != null ? request.getIsInactive() : card.getIsInactive()))
                .isDeleted(card.getIsDeleted())
                .expirationDate(card.getExpirationDate())
                .creationDate(card.getCreationDate())
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    public static OutputCard toOutputCard(Card card) {
        return OutputCard.builder()
                .id(card.getPublicId())
                .cardNumber(card.getCardNumber())
                .cardOwner(card.getCardOwner())
                .expirationDate(String.valueOf(card.getExpirationDate()))
                .cvv(card.getCvv())
                .pin(card.getPin())
                .cardType(CardTypeMapper.toOutputCardType(card.getCardType()))
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