package com.rewards.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rewards.dto.ErrorResponse;
import com.rewards.model.CustomerRewards;
import com.rewards.model.Transaction;
import com.rewards.service.RewardsService;

/*
 * REST Controller that exposes endpoints for the Rewards Program API.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

	private final RewardsService rewardsService;

	@Autowired
	public RewardsController(RewardsService rewardsService) {
		this.rewardsService = rewardsService;
	}

	/*
	 * Returns reward points for ALL customers over the last 3 months.
	 * 
	 * @return list of CustomerRewards or 204 if no data found
	 */
	@GetMapping("/all")
	public ResponseEntity<List<CustomerRewards>> getAllRewards() {
		List<CustomerRewards> rewards = rewardsService.getAllCustomerRewards();
		if (rewards.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(rewards);
	}

	/*
	 * Returns reward points for a specific customer over the last 3 months.
	 * 
	 * @param customerId the customer's unique ID
	 * 
	 * @return CustomerRewards or 404 if not found
	 */
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<?> getRewardsByCustomer(@PathVariable String customerId) {
		try {
			CustomerRewards rewardsByCustomer = rewardsService.getRewardsByCustomer(customerId);
			return ResponseEntity.ok(rewardsByCustomer);
		} catch (NoSuchElementException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse(404, "Not Found", e.getMessage()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorResponse(400, "Bad Request", e.getMessage()));
		}

	}

	/*
	 * Adds a new transaction (used for testing/demo data loading).
	 * 
	 * @param transaction the transaction object from request body
	 * 
	 * @return the saved transaction
	 */
	@PostMapping("/transaction")
	public ResponseEntity<?> addTransaction(@RequestBody Transaction transaction) {
		if (transaction.getCustomerId() == null || transaction.getCustomerId().isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorResponse(400, "Bad Request", "Customer ID must not be blank."));
		}
		if (transaction.getAmount() == null || transaction.getAmount().doubleValue() < 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorResponse(400, "Bad Request", "Amount must be zero or positive."));
		}
		if (transaction.getTransactionDate() == null || transaction.getTransactionDate().isAfter(LocalDate.now())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorResponse(400, "Bad Request", "Transaction date cannot be null or in the future."));
		}
		Transaction saved = rewardsService.addTransaction(transaction);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

}
