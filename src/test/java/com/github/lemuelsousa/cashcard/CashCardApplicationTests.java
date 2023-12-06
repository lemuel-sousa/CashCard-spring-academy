package com.github.lemuelsousa.cashcard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	private String username = "lemuk";
	private String password = "lemuk123";

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext responseBody = JsonPath.parse(response.getBody());

		Number id = responseBody.read("$.id");
		assertThat(id).isEqualTo(99);

		Double amount = responseBody.read("$.amount");
		assertThat(amount).isEqualTo(123.45);
	}

	@Test
	void shouldNotReturnACashCardWithAnUnknownId() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards/9999999", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldCreateACashCard() {
		var newCashCard = new CashCard(null, 360.00, null);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth(username, password)
				.postForEntity("/cashcards", newCashCard, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewCashCard = response.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth(username, password)
				.getForEntity(locationOfNewCashCard, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext bodyResponse = JsonPath.parse(getResponse.getBody());
		Number id = bodyResponse.read("$.id");
		Double amount = bodyResponse.read("$.amount");
		String owner = bodyResponse.read("$.owner");
		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(360);
		assertThat(owner).isEqualTo(username);
	}

	@Test
	void shoudlReturnAllCashCardsWhenListIsRequested() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext bodyResponse = JsonPath.parse(response.getBody());
		int cashCardCount = bodyResponse.read("$.length()");
		assertThat(cashCardCount).isEqualTo(3);

		JSONArray ids = bodyResponse.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

		JSONArray amouts = bodyResponse.read("$..amount");
		assertThat(amouts).containsExactlyInAnyOrder(123.45, 150.00, 200.00);
	}

	@Test
	void shouldReturnAPageOfCashCards() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext bodyResponse = JsonPath.parse(response.getBody());
		JSONArray page = bodyResponse.read("$[*]");
		assertThat(page.size()).isEqualTo(1);

		Double amount = bodyResponse.read("$[0].amount");
		assertThat(amount).isEqualTo(200.00);
	}

	@Test
	void shouldReturnASortedAPageOfCashCardsWithNoParametersAndUseDefaultValues() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext bodyResponse = JsonPath.parse(response.getBody());
		JSONArray page = bodyResponse.read("$[*]");
		assertThat(page.size()).isEqualTo(3);

		JSONArray amounts = bodyResponse.read("$..amount");
		var amountsInAscOrder = Collections.unmodifiableList(List.of(123.45, 150.00, 200.00));
		assertThat(amounts).containsExactlyElementsOf(amountsInAscOrder);
	}

	@Test
	void shouldNotReturnACashCardWhenUsingBadCredentials() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("BAD-USER", password)
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate
				.withBasicAuth(username, "BAD-PASSWORD")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhoAreNotCardOwners() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("unauthorized-user", "block123")
				.getForEntity("/cashcards", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
		ResponseEntity<String> response = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards/102", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldUpdateAnExistingCashCard() {
		var cashCardUpdate = new CashCard(null, 25.99, null);
		var request = new HttpEntity<>(cashCardUpdate);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth(username, password)
				.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext bodyResponse = JsonPath.parse(getResponse.getBody());
		Number id = bodyResponse.read("$.id");
		Double amount = bodyResponse.read("$.amount");
		String owner = bodyResponse.read("$.owner");
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(25.99);
		assertThat(owner).isEqualTo(username);
	}

	@Test
	void shouldNotUpdateAnExistingCashCardThatDoesNotExist() {
		var unknownCard = new CashCard(null, 25.99, null);
		var request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth(username, password)
				.exchange("/cashcards/9999", HttpMethod.PUT, request, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard() {
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth(username, password)
				.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth(username, password)
				.getForEntity("/cashcards/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteACashCardThatDoesNotExist() {
		ResponseEntity<Void> response = restTemplate
			.withBasicAuth(username, password)
			.exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}