package org.firek.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.assertj.core.api.Condition;
import org.firek.Account;
import org.firek.AccountsRepository;
import org.firek.Amount;
import org.firek.exceptions.NotEnoughMoneyOnAccountException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;

@RunWith(ConcurrentTestRunner.class)
public class ConcurrentTransfersShould {

    private static final Integer ACCOUNT_NUMBER = 101;
    private static final Amount ACCOUNT_BALANCE = new Amount(new BigDecimal("0.01"));
    private static final Account ACCOUNT = new Account(ACCOUNT_NUMBER, ACCOUNT_BALANCE);

    private static final Integer ANOTHER_ACCOUNT_NUMBER = 102;
    private static final Amount ANOTHER_ACCOUNT_BALANCE = new Amount(new BigDecimal("22.22"));
    private static final Account ANOTHER_ACCOUNT = new Account(ANOTHER_ACCOUNT_NUMBER, ANOTHER_ACCOUNT_BALANCE);

    private static final BigDecimal TRANSFER_AMOUNT = ACCOUNT_BALANCE.getAmount();

    private static final Condition<Amount> NOT_BELOW_ZERO = new Condition<>(
            ConcurrentTransfersShould::amountIsNotBelowZero, "Amount is not below zero");

    private AccountsRepository repository;

    @Before
    public void setUp() {
        repository = new AccountsRepository(ACCOUNT, ANOTHER_ACCOUNT);
    }

    @Test
    @ThreadCount(5)
    public void keep_accounts_balances_consistent() {
        Amount transferAmount = new Amount(TRANSFER_AMOUNT);

        try {
            repository.transfer(ACCOUNT_NUMBER, ANOTHER_ACCOUNT_NUMBER, transferAmount);
        } catch (NotEnoughMoneyOnAccountException ignoredExpectedException) {
        }
    }

    @After
    public void check_if_account_are_consistent_after_concurrent_transfers() {
        Amount expectedBalanceForTargetAccount = ANOTHER_ACCOUNT_BALANCE.add(ACCOUNT_BALANCE);

        assertThat(repository.getBalance(ACCOUNT_NUMBER)).usingComparator(Amount::compareTo)
                .is(NOT_BELOW_ZERO);
        assertThat(repository.getBalance(ANOTHER_ACCOUNT_NUMBER)).usingComparator(Amount::compareTo)
                .isEqualTo(expectedBalanceForTargetAccount);
    }

    private static boolean amountIsNotBelowZero(Amount amount) {
        return amount.compareTo(new Amount(BigDecimal.ZERO)) >= 0;
    }
}
