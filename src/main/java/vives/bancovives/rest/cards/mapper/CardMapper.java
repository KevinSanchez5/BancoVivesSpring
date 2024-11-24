package vives.bancovives.rest.cards.mapper;

import vives.bancovives.rest.cards.dto.input.InputCard;
import vives.bancovives.rest.cards.dto.input.UpdateRequestCard;
import vives.bancovives.rest.cards.dto.output.OutputCard;
import vives.bancovives.rest.cards.model.Card;

import java.time.LocalDateTime;

public class CardMapper {

    public static Card toCard(InputCard inputCard) {
        return Card.builder()
                .cardOwner(inputCard.getCardOwner().trim().toUpperCase())
                .pin(inputCard.getPin())
                //.cardType(inputCard.getCardType())
                .dailyLimit(inputCard.getDailyLimit())
                .weeklyLimit(inputCard.getWeeklyLimit())
                .monthlyLimit(inputCard.getMonthlyLimit())
                .build();
    }

    public static Card toCard(UpdateRequestCard request, Card card) {
        return Card.builder()
                .id(card.getId())
                .cardOwner(request.getCardOwner() != null ? request.getCardOwner() : card.getCardOwner())
                .pin(request.getPin() != null ? request.getPin() : card.getPin())
                .dailyLimit(request.getDailyLimit() != null ? request.getDailyLimit() : card.getDailyLimit())
                .weeklyLimit(request.getWeeklyLimit() != null ? request.getWeeklyLimit() : card.getWeeklyLimit())
                .monthlyLimit(request.getMonthlyLimit() != null ? request.getMonthlyLimit() : card.getMonthlyLimit())
                .cvv(card.getCvv())
                .cardNumber(card.getCardNumber())
                .isDeleted(card.getIsDeleted())
                .expirationDate(card.getExpirationDate())
                .creationDate(card.getCreationDate())
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    public static OutputCard toOutputCard(Card card) {
        return OutputCard.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .cardOwner(card.getCardOwner())
                .expirationDate(String.valueOf(card.getExpirationDate()))
                .cvv(card.getCvv())
                .pin(card.getPin())
                //.cardType(String.valueOf(card.getCardType()))
                .dailyLimit(card.getDailyLimit())
                .weeklyLimit(card.getWeeklyLimit())
                .monthlyLimit(card.getMonthlyLimit())
                .isDeleted(card.getIsDeleted())
                .creationDate(String.valueOf(card.getCreationDate()))
                .lastUpdate(String.valueOf(card.getLastUpdate()))
                .build();
    }
}