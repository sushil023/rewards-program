package com.rewards;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;

/*
 * Integration tests for RewardsController.
 * Uses @WithMockUser to simulate authenticated requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RewardsControllerTest {
	@Autowired
	private MockMvc mockMvc;

	// ── Unauthenticated Tests ────────────────────────────────────────────────

	@Test
	@DisplayName("GET /all without auth returns 401")
	void testGetAllRewards_noAuth_returns401() throws Exception {
		mockMvc.perform(get("/api/rewards/all")).andExpect(status().isUnauthorized());
	}

	// ── Authenticated Tests ──────────────────────────────────────────────────

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	@DisplayName("GET /all with auth returns 200")
	void testGetAllRewards_withAuth_returns200() throws Exception {
		mockMvc.perform(get("/api/rewards/all")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "user", roles = "USER")
	@DisplayName("GET /all response has correct JSON structure")
	void testGetAllRewards_hasCorrectStructure() throws Exception {
		mockMvc.perform(get("/api/rewards/all")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].customerId").exists()).andExpect(jsonPath("$[0].customerName").exists())
				.andExpect(jsonPath("$[0].totalPoints").exists()).andExpect(jsonPath("$[0].monthlyPoints").exists());
	}

	@Test
	@WithMockUser(username = "user", roles = "USER")
	@DisplayName("GET /customer/C001 returns Sushil's rewards")
	void testGetRewardsByCustomer_alice_returns200() throws Exception {
		mockMvc.perform(get("/api/rewards/customer/C001")).andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value("C001")).andExpect(jsonPath("$.customerName").value("Sushil"))
				.andExpect(jsonPath("$.totalPoints").isNumber()).andExpect(jsonPath("$.monthlyPoints").isMap());
	}

	@Test
	@WithMockUser(username = "user", roles = "USER")
	@DisplayName("GET /customer/INVALID returns 404 with ErrorResponse")
	void testGetRewardsByCustomer_invalid_returns404() throws Exception {
		mockMvc.perform(get("/api/rewards/customer/INVALID_ID")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	@DisplayName("POST /transaction valid data returns 201")
	void testAddTransaction_valid_returns201() throws Exception {
		String json = """
				{
				    "customerId": "C010",
				    "customerName": "TestUser",
				    "amount": 150.0,
				    "transactionDate": "%s"
				}
				""".formatted(LocalDate.now().minusMonths(1).toString());

		mockMvc.perform(post("/api/rewards/transaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.customerId").value("C010"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	@DisplayName("POST /transaction negative amount returns 400")
	void testAddTransaction_negativeAmount_returns400() throws Exception {
		String json = """
				{
				    "customerId": "C010",
				    "customerName": "TestUser",
				    "amount": -50.0,
				    "transactionDate": "%s"
				}
				""".formatted(LocalDate.now().minusMonths(1).toString());

		mockMvc.perform(post("/api/rewards/transaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	@DisplayName("POST /transaction future date returns 400")
	void testAddTransaction_futureDate_returns400() throws Exception {
		String json = """
				{
				    "customerId": "C010",
				    "customerName": "TestUser",
				    "amount": 100.0,
				    "transactionDate": "%s"
				}
				""".formatted(LocalDate.now().plusDays(5).toString());

		mockMvc.perform(post("/api/rewards/transaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	@DisplayName("POST then GET end-to-end flow works")
	void testAddTransaction_thenRetrieve_endToEnd() throws Exception {
		String json = """
				{
				    "customerId": "C020",
				    "customerName": "EndToEndUser",
				    "amount": 200.0,
				    "transactionDate": "%s"
				}
				""".formatted(LocalDate.now().minusMonths(1).toString());

		mockMvc.perform(post("/api/rewards/transaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/rewards/customer/C020")).andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value("C020")).andExpect(jsonPath("$.totalPoints").value(250));
	}
}
