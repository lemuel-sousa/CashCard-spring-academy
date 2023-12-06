package com.github.lemuelsousa.cashcard;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private ResponseEntity<CashCard> findById(@PathVariable Long id, Principal principal) {
        var cashCard = findCashCard(id, principal);
        if (cashCard != null)
            return ResponseEntity.ok(cashCard);

        return ResponseEntity.notFound().build();
    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))));
        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<Void> create(@RequestBody CashCard newCashCardRequest, Principal principal,
            UriComponentsBuilder uri) {
        var cashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard cashCardSaved = cashCardRepository.save(cashCardWithOwner);
        URI locationOfNewCashCard = uri
                .path("/cashcards/{id}")
                .buildAndExpand(cashCardSaved.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("{id}")
    private ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CashCard cashCardUpdated,
            Principal principal) {
        var cashCard = findCashCard(id, principal);
        if (cashCard != null) {
            var updatedCashCard = new CashCard(id, cashCardUpdated.amount(), principal.getName());
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("{id}")
    private ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        var cashCard = findCashCard(id, principal);
        if (cashCard != null) {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

}