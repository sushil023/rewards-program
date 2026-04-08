package com.rewards;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

			List<Transaction> transactions = List.of(
					// Customer 1 - Sushil
					new Transaction(null, "C001", "Sushil", BigDecimal.valueOf(120.0),
							now.minusMonths(2).withDayOfMonth(5)),
					new Transaction(null, "C001", "Sushil", BigDecimal.valueOf(75.0),
							now.minusMonths(2).withDayOfMonth(20)),
					new Transaction(null, "C001", "Sushil", BigDecimal.valueOf(200.0),
							now.minusMonths(1).withDayOfMonth(10)),
					new Transaction(null, "C001", "Sushil", BigDecimal.valueOf(30.0), now.withDayOfMonth(3)),

					// Customer 2 - Pramod
					new Transaction(null, "C002", "Pramod", BigDecimal.valueOf(55.0),
							now.minusMonths(2).withDayOfMonth(8)),
					new Transaction(null, "C002", "Pramod", BigDecimal.valueOf(130.0),
							now.minusMonths(1).withDayOfMonth(15)),
					new Transaction(null, "C002", "Pramod", BigDecimal.valueOf(95.0), now.withDayOfMonth(2)),

					// Customer 3 - Khiru
					new Transaction(null, "C003", "Khiru", BigDecimal.valueOf(250.0),
							now.minusMonths(2).withDayOfMonth(12)),
					new Transaction(null, "C003", "Khiru", BigDecimal.valueOf(45.0),
							now.minusMonths(1).withDayOfMonth(18)),
					new Transaction(null, "C003", "Khiru", BigDecimal.valueOf(110.0), now.withDayOfMonth(1)));

			repo.saveAll(transactions);
		};
	}
}
