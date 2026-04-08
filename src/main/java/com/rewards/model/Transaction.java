package com.rewards.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* 
 * Entity representing a customer's purchase transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions", indexes = { @Index(name = "idx_customer_id", columnList = "customerId"),
		@Index(name = "idx_transaction_date", columnList = "transactionDate"),
		@Index(name = "idx_customer_date", columnList = "customerId, transactionDate") })
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/* The ID of the customer who made the purchase. */
	@Column(nullable = false)
	private String customerId;

	/* The name of the customer. */
	@Column(nullable = false)
	private String customerName;

	/* The purchase amount in dollars. */
	@Column(nullable = false)
	private BigDecimal amount;

	/* The date the transaction occurred. */
	@Column(nullable = false)
	private LocalDate transactionDate;
}
