package com.github.lemuelsousa.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

@JsonTest
public class CashCardJsonContractTests {
    
    @Autowired
    JacksonTester<CashCard> json;


    @Test
    void cashCardSerializatinTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45);
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("$.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("$.id").isEqualTo(99);

        assertThat(json.write(cashCard)).hasJsonPathNumberValue("$.amount");
        assertThat(json.write(cashCard)).extractingJsonPathValue("$.amount").isEqualTo(123.45);
    }

    @Test
    void cashCardDeserializarionTest() throws IOException {
        String expected = """
            {
                "id": 99,
                "amount": 123.45
            }
        """;

        assertThat(json.parse(expected)).isEqualTo(new CashCard(99L, 123.45));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }
}
