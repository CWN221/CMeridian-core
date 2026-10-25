package com.bank.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchDTO {
    
    private String searchTerm;
    
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationalId;
    private String customerNumber;
    
    private String city;
    private String state;
    private String country;
    
    private Boolean isVerified;
    private Boolean isActive;
    
    private LocalDate dateOfBirthFrom;
    private LocalDate dateOfBirthTo;
    
    private LocalDate createdDateFrom;
    private LocalDate createdDateTo;
    
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
    
    // Helper method to check if any search criteria is provided
    public boolean hasSearchCriteria() {
        return searchTerm != null && !searchTerm.trim().isEmpty() ||
               firstName != null && !firstName.trim().isEmpty() ||
               lastName != null && !lastName.trim().isEmpty() ||
               email != null && !email.trim().isEmpty() ||
               phone != null && !phone.trim().isEmpty() ||
               nationalId != null && !nationalId.trim().isEmpty() ||
               customerNumber != null && !customerNumber.trim().isEmpty() ||
               city != null && !city.trim().isEmpty() ||
               state != null && !state.trim().isEmpty() ||
               country != null && !country.trim().isEmpty() ||
               isVerified != null ||
               isActive != null ||
               dateOfBirthFrom != null ||
               dateOfBirthTo != null ||
               createdDateFrom != null ||
               createdDateTo != null;
    }
    
    // Builder method for simple search
    public static CustomerSearchDTO simpleSearch(String searchTerm) {
        return CustomerSearchDTO.builder().searchTerm(searchTerm).build();
    }
    
    // Builder method for advanced search with pagination
    public static CustomerSearchDTO advancedSearch(String searchTerm, 
                                                    String firstName, 
                                                    String lastName, 
                                                    String email,
                                                    String phone,
                                                    String nationalId,
                                                    Boolean isVerified,
                                                    Boolean isActive,
                                                    Integer page,
                                                    Integer size) {
        return CustomerSearchDTO.builder()
                .searchTerm(searchTerm)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .nationalId(nationalId)
                .isVerified(isVerified)
                .isActive(isActive)
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
    }
}