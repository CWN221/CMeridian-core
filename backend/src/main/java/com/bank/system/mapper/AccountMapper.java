package com.bank.system.mapper;

import com.bank.system.dto.AccountDTO;
import com.bank.system.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Mapper responsible for converting Account entities into AccountDTOs.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Converts an Account entity into an AccountDTO.
     *
     * @param account Account entity to convert
     * @return AccountDTO containing account and related information
     */
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName",source = "customer",qualifiedByName = "customerFullName")
    @Mapping(target = "accountTypeCode", source = "accountType.typeCode")
    @Mapping(target = "accountTypeName", source = "accountType.typeName")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "currencyCode", source = "currency.currencyCode")
    @Mapping(target = "status", source = "status")
    AccountDTO toDTO(Account account);


    /**
     * Converts a list of Account entities into a list of AccountDTOs.
     *
     * @param accounts list of Account entities
     * @return list of AccountDTO objects
     */
    List<AccountDTO> toDTOList(List<Account> accounts);


    /**
     * Builds the customer's display name for the AccountDTO.     
     *
     * @param customer customer associated with the account
     * @return customer's full name or null when no customer is available
     */
    @Named("customerFullName")
    default String getCustomerFullName(com.bank.system.entity.Customer customer) {
        return customer != null ? customer.getFullName() : null;
    }
}
