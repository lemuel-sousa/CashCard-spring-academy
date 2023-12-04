package com.github.lemuelsousa.cashcard;

import org.springframework.data.annotation.Id;

public record CashCard(@Id Long id, Double amount) {

}
