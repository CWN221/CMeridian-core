package com.bank.system.mapper;

import com.bank.system.dto.TransactionDTO;
import com.bank.system.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper responsible for converting Transaction entities into TransactionDTO objects.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Converts a Transaction entity into a TransactionDTO.
     *
     * @param transaction transaction entity to convert
     * @return TransactionDTO containing transaction information
     */
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountNumber", source = "account.accountNumber")
    @Mapping(target = "currencyCode", source = "currency.currencyCode")
    @Mapping(target = "counterpartyAccountNumber", source = "counterpartyAccountId")
    @Mapping(target = "counterpartyCustomerName", source = "counterpartyCustomerId")
    @Mapping(target = "relatedTransactionId", source = "relatedTransaction.id")
    TransactionDTO toDTO(Transaction transaction);


    /**
     * Converts a list of Transaction entities into a list of DTOs.
     *
     * @param transactions list of transaction entities
     * @return list of TransactionDTO objects
     */
    List<TransactionDTO> toDTOList(List<Transaction> transactions);


    /**
     * Converts a user's account ID into a displayable account number.
     *
     * @param accountId user's account ID
     * @return formatted account number or null when the ID is null
     */
    default String mapCounterpartyAccountNumber(Long accountId) {
        return accountId != null ? "ACC-" + accountId : null;
    }
}
