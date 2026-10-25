package com.bank.system.service;

import com.bank.system.dto.CustomerDTO;
import com.bank.system.entity.Customer;
import com.bank.system.exception.BusinessException;
import com.bank.system.exception.ResourceNotFoundException;
import com.bank.system.mapper.CustomerMapper;
import com.bank.system.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public Page<CustomerDTO> listCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
        return customerMapper.toDTO(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByNumber(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerNumber));
        return customerMapper.toDTO(customer);
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO dto) {
        log.info("Creating customer with email: {}", dto.getEmail());

        if (customerRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException("Customer with email " + dto.getEmail() + " already exists");
        }
        if (customerRepository.findByNationalId(dto.getNationalId()).isPresent()) {
            throw new BusinessException("Customer with national ID " + dto.getNationalId() + " already exists");
        }

        dto.setCustomerNumber(generateCustomerNumber());

        Customer customer = customerMapper.toEntity(dto);
        customer.setIsActive(true);
        customer.setIsVerified(false);
        customer.setCreatedBy(currentUsername());

        Customer saved = customerRepository.save(customer);
        log.info("Customer created: id={}, number={}", saved.getId(), saved.getCustomerNumber());
        return customerMapper.toDTO(saved);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        log.info("Updating customer: {}", id);

        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            customerRepository.findByEmail(dto.getEmail()).ifPresent(other -> {
                throw new BusinessException("Email " + dto.getEmail() + " belongs to another customer");
            });
        }
        if (dto.getNationalId() != null && !dto.getNationalId().equals(existing.getNationalId())) {
            customerRepository.findByNationalId(dto.getNationalId()).ifPresent(other -> {
                throw new BusinessException("National ID " + dto.getNationalId() + " belongs to another customer");
            });
        }

        customerMapper.updateEntity(dto, existing);
        existing.setUpdatedBy(currentUsername());

        Customer saved = customerRepository.save(existing);
        log.info("Customer updated: {}", saved.getId());
        return customerMapper.toDTO(saved);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Soft deleting customer: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        customer.setIsActive(false);
        customer.setUpdatedBy(currentUsername());
        customerRepository.save(customer);
        log.info("Customer deactivated: {}", id);
    }

    @Transactional
    public CustomerDTO verifyCustomer(Long id, String remarks) {
        log.info("Verifying customer: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

        customer.setIsVerified(true);
        customer.setVerificationDate(LocalDateTime.now());
        customer.setVerificationRemarks(remarks);
        customer.setUpdatedBy(currentUsername());

        return customerMapper.toDTO(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public long countActiveCustomers() {
        return customerRepository.countActiveCustomers();
    }

    private String generateCustomerNumber() {
        return "CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}