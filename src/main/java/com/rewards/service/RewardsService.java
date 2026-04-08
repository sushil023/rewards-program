package com.rewards.service;

import java.util.List;

import com.rewards.model.CustomerRewards;
import com.rewards.model.Transaction;

/*
 * Service Interface for achieving abstraction and it's implementation class
 * contains the complete business logic.
 */
public interface RewardsService {

	long calculatePoints(double amount);

	List<CustomerRewards> getAllCustomerRewards();

	CustomerRewards getRewardsByCustomer(String customerId);

	Transaction addTransaction(Transaction transaction);

}
