package com.bank.system.mapper;

import com.bank.system.dto.CardDTO;
import com.bank.system.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(card.getCustomer() != null ? card.getCustomer().getFullName() : null)")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountNumber", source = "account.accountNumber")
    CardDTO toDTO(Card card);

    List<CardDTO> toDTOList(List<Card> cards);
}