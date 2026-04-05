package com.rewards.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
 * Response model that holds the calculated reward points
 * for a customer, broken down by month and total.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRewards {
	
	private String customerId;

    /* The name of the customer. */
    private String customerName;

    /*
     * A map of month-year (e.g., "JANUARY-2024") to
     * points earned in that month.
     */
    private Map<String, Long> monthlyPoints;

    /* Total reward points earned across all months. */
    private Long totalPoints;
}
