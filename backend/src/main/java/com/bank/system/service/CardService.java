package com.bank.system.service;

import com.bank.system.dto.CardDTO;
import com.bank.system.dto.CardIssueRequestDTO;
import com.bank.system.entity.Account;
import com.bank.system.entity.Card;
import com.bank.system.entity.Customer;
import com.bank.system.exception.BusinessException;
import com.bank.system.exception.ResourceNotFoundException;
import com.bank.system.mapper.CardMapper;
import com.bank.system.repository.AccountRepository;
import com.bank.system.repository.CardRepository;
import com.bank.system.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardMapper cardMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<CardDTO> list(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<CardDTO> byCustomer(Long customerId) {
        return cardRepository.findByCustomerId(customerId).stream()
                .map(cardMapper::toDTO)
                .toList();
    }

    @Transactional
    public CardDTO issue(CardIssueRequestDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String cardNumber = generateCardNumber(dto.getCardBrand());
        String cvv = String.format("%03d", new SecureRandom().nextInt(1000));

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .account(account)
                .customer(customer)
                .cardType(Card.CardType.valueOf(dto.getCardType()))
                .cardBrand(Card.CardBrand.valueOf(dto.getCardBrand()))
                .cardholderName(customer.getFullName())
                .expiryDate(LocalDate.now().plusYears(4))
                .issueDate(LocalDate.now())
                .cvvHash(passwordEncoder.encode(cvv))
                .dailyLimit(dto.getDailyLimit())
                .monthlyLimit(dto.getMonthlyLimit())
                .singleTransactionLimit(dto.getSingleTransactionLimit())
                .status(Card.CardStatus.ACTIVE)
                .pinAttempts(0)
                .createdBy(currentUsername())
                .build();

        return cardMapper.toDTO(cardRepository.save(card));
    }

    @Transactional
    public CardDTO block(Long id, String reason) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new BusinessException("Only active cards can be blocked");
        }
        card.setStatus(Card.CardStatus.BLOCKED);
        card.setBlockedReason(reason);
        card.setUpdatedBy(currentUsername());
        return cardMapper.toDTO(cardRepository.save(card));
    }

    @Transactional
    public CardDTO unblock(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
        if (card.getStatus() != Card.CardStatus.BLOCKED) {
            throw new BusinessException("Only blocked cards can be unblocked");
        }
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setBlockedReason(null);
        card.setUpdatedBy(currentUsername());
        return cardMapper.toDTO(cardRepository.save(card));
    }

    @Transactional
    public CardDTO cancel(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
        card.setStatus(Card.CardStatus.CANCELLED);
        card.setUpdatedBy(currentUsername());
        return cardMapper.toDTO(cardRepository.save(card));
    }

    private String generateCardNumber(String brand) {
        String prefix = "MASTERCARD".equals(brand) ? "5412" : "4532";
        StringBuilder sb = new StringBuilder(prefix);
        SecureRandom rnd = new SecureRandom();
        while (sb.length() < 16) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}