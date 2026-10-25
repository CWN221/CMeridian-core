package com.bank.system.controller;

import com.bank.system.dto.CardDTO;
import com.bank.system.dto.CardIssueRequestDTO;
import com.bank.system.service.CardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @GetMapping
    @PreAuthorize("hasAuthority('CARD_VIEW')")
    public ResponseEntity<Page<CardDTO>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(cardService.list(pageable));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('CARD_VIEW')")
    public ResponseEntity<List<CardDTO>> byCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(cardService.byCustomer(customerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CARD_ISSUE')")
    public ResponseEntity<CardDTO> issue(@Valid @RequestBody CardIssueRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.issue(dto));
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasAuthority('CARD_BLOCK')")
    public ResponseEntity<CardDTO> block(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Blocked by staff") String reason) {
        return ResponseEntity.ok(cardService.block(id, reason));
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasAuthority('CARD_UNBLOCK')")
    public ResponseEntity<CardDTO> unblock(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.unblock(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('CARD_CLOSE')")
    public ResponseEntity<CardDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.cancel(id));
    }
}