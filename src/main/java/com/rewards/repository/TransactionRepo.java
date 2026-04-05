package com.rewards.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rewards.model.Transaction;

/*
 * Repository interface for performing the CRUD operations
 */

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {
	 /*
     * Finds all transactions within a given date range.
     * @param startDate the start of the date range 
     * @param endDate   the end of the date range 
     * @return list of transactions within the range
     */
	List<Transaction> findByTransactionDateBetween(LocalDate startDate,LocalDate enddate);
	
	/*
     * Finds transactions for a customer within a given date range.
     * @param customerId the customer's unique ID
     * @param startDate  the start of the date range 
     * @param endDate    the end of the date range
     * @return filtered list of transactions
     */
	List<Transaction> findByCustomerIdAndTransactionDateBetween(String customerid,LocalDate startDate,LocalDate enddate);

}
