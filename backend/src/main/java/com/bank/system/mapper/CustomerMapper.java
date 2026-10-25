package com.bank.system.mapper;

import com.bank.system.dto.CustomerDTO;
import com.bank.system.entity.Customer;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper responsible for converting between Customer entities and CustomerDTO objects.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {

    /**
     * Converts a CustomerDTO into a new Customer entity.
     *
     * System-managed fields such as ID & version are intentionally ignored because they are managed by the service layer.
     *
     * @param customerDTO customer data received from the client
     * @return a new Customer entity populated from the DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "branches", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "beneficiaries", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    Customer toEntity(CustomerDTO customerDTO);


    /**
     * Converts a Customer entity into a CustomerDTO.
     *
     * All fields required by the API response are explicitly mapped
     * to make the DTO structure clear and prevent accidental exposure of entity relationships.
     *
     * @param customer Customer entity
     * @return CustomerDTO containing customer information
     */
    @Mapping(source = "customer.id", target = "id")
    @Mapping(source = "customer.customerNumber", target = "customerNumber")
    @Mapping(source = "customer.firstName", target = "firstName")
    @Mapping(source = "customer.lastName", target = "lastName")
    @Mapping(source = "customer.middleName", target = "middleName")
    @Mapping(source = "customer.dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "customer.nationalId", target = "nationalId")
    @Mapping(source = "customer.passportNumber", target = "passportNumber")
    @Mapping(source = "customer.email", target = "email")
    @Mapping(source = "customer.phone", target = "phone")
    @Mapping(source = "customer.alternatePhone", target = "alternatePhone")
    @Mapping(source = "customer.addressLine1", target = "addressLine1")
    @Mapping(source = "customer.addressLine2", target = "addressLine2")
    @Mapping(source = "customer.city", target = "city")
    @Mapping(source = "customer.state", target = "state")
    @Mapping(source = "customer.postalCode", target = "postalCode")
    @Mapping(source = "customer.country", target = "country")
    @Mapping(source = "customer.occupation", target = "occupation")
    @Mapping(source = "customer.employer", target = "employer")
    @Mapping(source = "customer.annualIncome", target = "annualIncome")
    @Mapping(source = "customer.isVerified", target = "isVerified")
    @Mapping(source = "customer.verificationDate", target = "verificationDate")
    @Mapping(source = "customer.verificationRemarks", target = "verificationRemarks")
    @Mapping(source = "customer.isActive", target = "isActive")
    @Mapping(source = "customer.createdAt", target = "createdAt")
    @Mapping(source = "customer.updatedAt", target = "updatedAt")
    @Mapping(source = "customer.createdBy", target = "createdBy")
    @Mapping(source = "customer.updatedBy", target = "updatedBy")
    CustomerDTO toDTO(Customer customer);


    /**
     * Converts a list of Customer entities into a list of CustomerDTOs.
     *
     * @param customers list of Customer entities
     * @return list of CustomerDTO objects
     */
    List<CustomerDTO> toDTOList(List<Customer> customers);


    /**
     * Updates an existing Customer entity using values from a CustomerDTO.     
     *
     * Database-managed fields and customer relationships are protected from being overwritten during the update.
     *
     * @param customerDTO DTO containing the fields to update
     * @param customer existing Customer entity to be updated
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    // Relationships are  excluded from partial updates
    @Mapping(target = "branches", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "beneficiaries", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEntity(CustomerDTO customerDTO, @MappingTarget Customer customer);
}
