package com.rewards;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.http.MediaType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class RewardsControllerTest {
	@Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllRewards_returnsOk() throws Exception {
        mockMvc.perform(get("/api/rewards/all"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRewardsByCustomer_wirhvalidId() throws Exception {
        mockMvc.perform(get("/api/rewards/customer/C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("C001"));
    }

    @Test
    void testGetRewardsByCustomer_withinvalidId_returns404() throws Exception {
        mockMvc.perform(get("/api/rewards/customer/INVALID_ID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddTransaction_success() throws Exception {
        String json = """
                {
                    "customerId": "C004",
                    "customerName": "Dev",
                    "amount": 150.0,
                    "transactionDate": "%s"
                }
                """.formatted(java.time.LocalDate.now().minusMonths(1));

        mockMvc.perform(post("/api/rewards/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("C004"));
    }
}
