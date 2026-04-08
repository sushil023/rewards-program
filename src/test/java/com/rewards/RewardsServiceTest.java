package com.rewards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rewards.model.CustomerRewards;
import com.rewards.model.Transaction;
import com.rewards.repository.TransactionRepo;
import com.rewards.service.RewardsServiceImpl;

/*
 * Unit tests for RewardsService contains point calculation logic,
 * edge cases, and exception scenarios.
 */

public class RewardsServiceTest {

    @Mock
    private TransactionRepo transactionRepository;

    @InjectMocks
    private RewardsServiceImpl rewardsService;   

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /*
     * Point Calculation Tests
     */

    @Test
    @DisplayName("$120 purchase should earn 90 points")
    void testCalculatePoints_120dollars_returns90() {
        assertEquals(90, rewardsService.calculatePoints(120.0));
    }

    @Test
    @DisplayName("$200 purchase should earn 250 points")
    void testCalculatePoints_200dollars_returns250() {
        assertEquals(250, rewardsService.calculatePoints(200.0));
    }

    @Test
    @DisplayName("$75 purchase should earn 25 points")
    void testCalculatePoints_75dollars_returns25() {
        assertEquals(25, rewardsService.calculatePoints(75.0));
    }

    @Test
    @DisplayName("$100 purchase should earn 50 points")
    void testCalculatePoints_exactly100_returns50() {
        assertEquals(50, rewardsService.calculatePoints(100.0));
    }

    @Test
    @DisplayName("$50 purchase should earn 0 points (boundary)")
    void testCalculatePoints_exactly50_returns0() {
        assertEquals(0, rewardsService.calculatePoints(50.0));
    }

    @Test
    @DisplayName("$30 purchase should earn 0 points")
    void testCalculatePoints_below50_returns0() {
        assertEquals(0, rewardsService.calculatePoints(30.0));
    }

    @Test
    @DisplayName("$0 purchase should earn 0 points")
    void testCalculatePoints_zero_returns0() {
        assertEquals(0, rewardsService.calculatePoints(0.0));
    }

    @Test
    @DisplayName("Negative amount should throw IllegalArgumentException")
    void testCalculatePoints_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardsService.calculatePoints(-10.0));
    }

    @Test
    @DisplayName("$101 purchase should earn 52 points")
    void testCalculatePoints_justAbove100_returns52() {
        assertEquals(52, rewardsService.calculatePoints(101.0));
    }
    
    @Test
    @DisplayName("$100.50 should earn 51 points (floating point edge case)")
    void testCalculatePoints_100dollars50cents_returns51() {
        assertEquals(50, rewardsService.calculatePoints(100.50));
    }
    
    @Test
    @DisplayName("$50.99 should earn 0 points (just above $50 boundary)")
    void testCalculatePoints_50dollars99cents_returns0() {
        assertEquals(0, rewardsService.calculatePoints(50.99));
    }

    @Test
    @DisplayName("$51.00 should earn 1 point")
    void testCalculatePoints_51dollars_returns1() {
        assertEquals(1, rewardsService.calculatePoints(51.0));
    }

    /*
     * Service Logic Tests
     * 
     */

    @Test
    @DisplayName("getRewardsByCustomer returns correct data for valid customer")
    void testGetRewardsByCustomer_validCustomer_returnsRewards() {
        LocalDate now = LocalDate.now();
        List<Transaction> mockTransactions = List.of(
                new Transaction(null,"C001", "Sushil", BigDecimal.valueOf(120.0), now.minusMonths(1)),
                new Transaction(null,"C001", "Sushil", BigDecimal.valueOf(75.0), now.minusMonths(2))
        );

        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                any(), any(), any())).thenReturn(mockTransactions);

        CustomerRewards rewards = rewardsService.getRewardsByCustomer("C001");

        assertNotNull(rewards);
        assertEquals("C001", rewards.getCustomerId());
        assertEquals("Sushil", rewards.getCustomerName());
        assertEquals(115L, rewards.getTotalPoints()); // 90 + 25
    }

    @Test
    @DisplayName("getRewardsByCustomer passes correct date range to repository")
    void testGetRewardsByCustomer_passesCorrectDateRange() {
        LocalDate now = LocalDate.now();
        LocalDate expectedStart = now.minusMonths(3).withDayOfMonth(1);

        List<Transaction> mockTransactions = List.of(
                new Transaction(null,"C001", "Alice", BigDecimal.valueOf(120.0),
                        now.minusMonths(1).withDayOfMonth(10))
        );

        ArgumentCaptor<LocalDate> startCaptor =
                ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq("C001"),
                startCaptor.capture(),
                endCaptor.capture()))
                .thenReturn(mockTransactions);

        rewardsService.getRewardsByCustomer("C001");

        // Verify the correct start date (1st of 3 months ago)
        assertEquals(expectedStart, startCaptor.getValue());
    }
    
    @Test
    @DisplayName("getRewardsByCustomer throws NoSuchElementException for unknown customer")
    void testGetRewardsByCustomer_unknownCustomer_throwsException() {
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                any(), any(), any())).thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class,
                () -> rewardsService.getRewardsByCustomer("UNKNOWN"));
    }

    @Test
    @DisplayName("getRewardsByCustomer throws IllegalArgumentException for blank ID")
    void testGetRewardsByCustomer_blankId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardsService.getRewardsByCustomer(""));
    }

    @Test
    @DisplayName("getRewardsByCustomer throws IllegalArgumentException for null ID")
    void testGetRewardsByCustomer_nullId_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardsService.getRewardsByCustomer(null));
    }

    @Test
    @DisplayName("getAllCustomerRewards returns empty list when no transactions exist")
    void testGetAllCustomerRewards_noData_returnsEmptyList() {
        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        List<CustomerRewards> result = rewardsService.getAllCustomerRewards();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAllCustomerRewards returns rewards for multiple customers")
    void testGetAllCustomerRewards_multipleCustomers_returnsAll() {
        LocalDate now = LocalDate.now();
        List<Transaction> mockTransactions = List.of(
                new Transaction(null,"C001", "Sushil", BigDecimal.valueOf(120.0), now.minusMonths(1).withDayOfMonth(5)),
                new Transaction(null,"C002", "Pramod",   BigDecimal.valueOf(200.0), now.minusMonths(1).withDayOfMonth(6)),
                new Transaction(null,"C003", "Khiru", BigDecimal.valueOf(75.0),  now.minusMonths(2).withDayOfMonth(7))
        );

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(mockTransactions);

        List<CustomerRewards> result = rewardsService.getAllCustomerRewards();
        assertEquals(3, result.size());
    }
    
    @Test
    @DisplayName("addTransaction saves and returns transaction")
    void testSaveTransaction_validTransaction_returnsSaved() {
        LocalDate now = LocalDate.now();
        Transaction tx = new Transaction(null,"C001", "Sushil",
        		BigDecimal.valueOf(100.0), now.minusMonths(1));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(tx);

        Transaction result = rewardsService.addTransaction(tx);

        assertNotNull(result);
        assertEquals("C001", result.getCustomerId());
        verify(transactionRepository, times(1)).save(tx);
    }
    
    @Test
    @DisplayName("Transaction exactly on startDate boundary should be included")
    void testGetRewardsByCustomer_transactionOnStartDate_isIncluded() {
        LocalDate startDate = LocalDate.now()
                .minusMonths(3).withDayOfMonth(1);

        List<Transaction> mockTransactions = List.of(
                new Transaction(null,"C001", "Alice", BigDecimal.valueOf(120.0), startDate)
        );

        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq("C001"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockTransactions);

        CustomerRewards rewards = rewardsService.getRewardsByCustomer("C001");

        assertNotNull(rewards);
        assertEquals(90L, rewards.getTotalPoints());
    }
}
