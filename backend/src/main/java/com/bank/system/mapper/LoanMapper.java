package com.bank.system.mapper;

import com.bank.system.dto.LoanDTO;
import com.bank.system.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(loan.getCustomer() != null ? loan.getCustomer().getFullName() : null)")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountNumber", source = "account.accountNumber")
    LoanDTO toDTO(Loan loan);

    List<LoanDTO> toDTOList(List<Loan> loans);
}