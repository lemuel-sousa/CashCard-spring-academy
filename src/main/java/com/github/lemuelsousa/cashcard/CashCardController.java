package com.github.lemuelsousa.cashcard;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{id}")
    private ResponseEntity<CashCard> findById(@PathVariable Long id) {
        Optional<CashCard> cashCard = cashCardRepository.findById(id);
        if (cashCard.isPresent())
            return ResponseEntity.ok(cashCard.get());

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<Void> create(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder uri) {
        CashCard cashCardSaved = cashCardRepository.save(newCashCardRequest);;
        URI locationOfNewCashCard = uri
            .path("/cashcards/{id}")    
            .buildAndExpand(cashCardSaved.id())
            .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

}