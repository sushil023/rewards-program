package com.rewards;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepo;

/*
 * Loads sample transaction data on application startup
 * to demonstrate the rewards calculation API.
 */
@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner loadData(TransactionRepo repo) {
		return args -> {
			LocalDate now = LocalDate.now();

			// Customer 1 - Sushil
			repo.save(new Transaction(null,"C001", "Sushil", 120.0, now.minusMonths(2).withDayOfMonth(5)));
			repo.save(new Transaction(null,"C001", "Sushil", 75.0, now.minusMonths(2).withDayOfMonth(20)));
			repo.save(new Transaction(null,"C001", "Sushil", 200.0, now.minusMonths(1).withDayOfMonth(10)));
			repo.save(new Transaction(null,"C001", "Sushil", 30.0, now.withDayOfMonth(3)));

			// Customer 2 - Pramod
			repo.save(new Transaction(null,"C002", "Pramod", 55.0, now.minusMonths(2).withDayOfMonth(8)));
			repo.save(new Transaction(null,"C002", "Pramod", 130.0, now.minusMonths(1).withDayOfMonth(15)));
			repo.save(new Transaction(null,"C002", "Pramod", 95.0, now.withDayOfMonth(2)));

			// Customer 3 - Khiru
			repo.save(new Transaction(null,"C003", "Khiru", 250.0, now.minusMonths(2).withDayOfMonth(12)));
			repo.save(new Transaction(null,"C003", "Khiru", 45.0, now.minusMonths(1).withDayOfMonth(18)));
			repo.save(new Transaction(null,"C003", "Khiru", 110.0, now.withDayOfMonth(1)));
		};
	}
}
