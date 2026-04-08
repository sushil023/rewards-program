package com.rewards.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rewards.model.CustomerRewards;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepo;

/*
 * Service Implementation class containing the business logic for calculating
 * customer reward points based on transaction history.
 */

@Service
public class RewardsServiceImpl implements RewardsService {

	private final TransactionRepo transactionRepo;

	@Autowired
	public RewardsServiceImpl(TransactionRepo transactionRepo) {
		this.transactionRepo = transactionRepo;
	}

	/*
	 * Retrieves reward points for all customers over the last 3 months
	 * 
	 * @return list of CustomerRewards for all customers
	 */

	@Override
	public List<CustomerRewards> getAllCustomerRewards() {
		LocalDate endDate = LocalDate.now().minusMonths(0).withDayOfMonth(1).minusDays(1);
		LocalDate startDate = LocalDate.now().minusMonths(3).withDayOfMonth(1);

		List<Transaction> allTransactions = transactionRepo.findByTransactionDateBetween(startDate, endDate);

		if (allTransactions.isEmpty()) {
			return Collections.emptyList();
		}

		return groupAndCalculate(allTransactions);
	}

	/*
	 * Adds a new transaction to the db
	 * 
	 * @return saved transaction
	 */
	@Override
	public Transaction addTransaction(Transaction transaction) {
		Transaction savedTransaction = transactionRepo.save(transaction);
		return savedTransaction;
	}

	/*
	 * Retrieves reward points for a specific customer over the last 3 months.
	 * 
	 * @param customerId the customer's unique ID
	 * 
	 * @return CustomerRewards for the specified customer
	 * 
	 * @throws NoSuchElementException if no transactions found for the customer
	 */
	@Override
	public CustomerRewards getRewardsByCustomer(String customerId) {
		if (customerId == null || customerId.isBlank()) {
			throw new IllegalArgumentException("Customer ID must not be blank.");
		}

		LocalDate endDate = LocalDate.now().withDayOfMonth(1).minusDays(1);
		LocalDate startDate = LocalDate.now().minusMonths(3).withDayOfMonth(1);

		List<Transaction> transactions = transactionRepo.findByCustomerIdAndTransactionDateBetween(customerId,
				startDate, endDate);

		if (transactions.isEmpty()) {
			throw new NoSuchElementException("No transactions found for customer: " + customerId);
		}

		return groupAndCalculate(transactions).get(0);
	}

	/*
	 * Calculates reward points for a single transaction amount.
	 * 
	 * @param amount the purchase amount in dollars
	 * 
	 * @return the number of reward points earned
	 * 
	 * @throws IllegalArgumentException if the amount is negative
	 */

	@Override
	public long calculatePoints(double amount) {

		if (amount < 0) {
			throw new IllegalArgumentException("The Amount can't be negative");
		}
		BigDecimal bdAmount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);

		long points = 0;

		if (bdAmount.compareTo(BigDecimal.valueOf(100)) > 0) {
			// Points for dollars above $100 (× 2)
			BigDecimal above100 = bdAmount.subtract(BigDecimal.valueOf(100)).setScale(0, RoundingMode.FLOOR);
			points += above100.longValue() * 2;
			points += 50;

		} else if (bdAmount.compareTo(BigDecimal.valueOf(50)) > 0) {
			// Points for dollars above $50 (× 1)
			BigDecimal above50 = bdAmount.subtract(BigDecimal.valueOf(50)).setScale(0, RoundingMode.FLOOR);
			points += above50.longValue();
		}
		return points;
	}

	/*
	 * Groups all transactions by customer and month, then calculates points.
	 * 
	 * @param transactions list of transactions to process
	 * 
	 * @return list of CustomerRewards objects
	 */
	private List<CustomerRewards> groupAndCalculate(List<Transaction> transactions) {
		// Group by customerId
		Map<String, List<Transaction>> byCustomer = transactions.stream()
				.collect(Collectors.groupingBy(Transaction::getCustomerId));

		List<CustomerRewards> result = new ArrayList<>();

		for (Map.Entry<String, List<Transaction>> entry : byCustomer.entrySet()) {
			String customerId = entry.getKey();
			List<Transaction> customerTxns = entry.getValue();
			String customerName = customerTxns.get(0).getCustomerName();

			// Group by MONTH-YEAR key
			Map<String, Long> monthlyPoints = customerTxns.stream()
					.collect(Collectors.groupingBy(
							t -> t.getTransactionDate().format(DateTimeFormatter.ofPattern("MMMM-yyyy")).toUpperCase(),
							Collectors.summingLong(t -> calculatePoints(t.getAmount().doubleValue()))));

			long totalPoints = monthlyPoints.values().stream().mapToLong(Long::longValue).sum();

			result.add(new CustomerRewards(customerId, customerName, monthlyPoints, totalPoints));
		}

		return result;
	}

}
