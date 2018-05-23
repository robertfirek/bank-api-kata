package org.firek;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.math.BigDecimal;

import org.firek.exceptions.AccountNotFoundException;
import org.firek.exceptions.NotEnoughMoneyOnAccountException;
import org.junit.Before;
import org.junit.Test;

public class AccountsRepositoryShould {

    private static final Integer ACCOUNT_NUMBER = 101;
    private static final Amount ACCOUNT_BALANCE = new Amount(new BigDecimal("11.11"));
    private static final Account ACCOUNT = new Account(ACCOUNT_NUMBER, ACCOUNT_BALANCE);

    private static final Integer ANOTHER_ACCOUNT_NUMBER = 102;
    private static final Amount ANOTHER_ACCOUNT_BALANCE = new Amount(new BigDecimal("22.22"));
    private static final Account ANOTHER_ACCOUNT = new Account(ANOTHER_ACCOUNT_NUMBER, ANOTHER_ACCOUNT_BALANCE);

    private AccountsRepository repository;

    @Before
    public void setUp() {
        repository = new AccountsRepository(ACCOUNT, ANOTHER_ACCOUNT);
    }

    @Test
    public void return_balance_of_stored_accounts() {
        assertThat(repository.getBalance(ACCOUNT_NUMBER)).isEqualTo(ACCOUNT_BALANCE);
        assertThat(repository.getBalance(ANOTHER_ACCOUNT_NUMBER)).isEqualTo(ANOTHER_ACCOUNT_BALANCE);
    }

    @Test
    public void not_return_balance_for_non_existing_accounts() {
        Integer nonExistingAccountNumber = 0;

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> repository.getBalance(nonExistingAccountNumber));
    }

    @Test
    public void transfer_money_from_source_account_to_target_account() {
        Amount transferAmount = new Amount(new BigDecimal("10"));

        repository.transfer(ACCOUNT_NUMBER, ANOTHER_ACCOUNT_NUMBER, transferAmount);

        assertThat(repository.getBalance(ACCOUNT_NUMBER)).usingComparator(Amount::compareTo)
                .isEqualTo(ACCOUNT_BALANCE.subtract(transferAmount));
        assertThat(repository.getBalance(ANOTHER_ACCOUNT_NUMBER)).usingComparator(Amount::compareTo)
                .isEqualTo(ANOTHER_ACCOUNT_BALANCE.add(transferAmount));
    }

    @Test
    public void not_transfer_money_from_source_account_to_target_account_when_source_account_does_not_have_enough_money() {
        Amount tooBigTransferAmount = new Amount(new BigDecimal("1000000"));

        assertThatExceptionOfType(NotEnoughMoneyOnAccountException.class)
                .isThrownBy(() -> repository.transfer(ACCOUNT_NUMBER, ANOTHER_ACCOUNT_NUMBER, tooBigTransferAmount));

        assertThat(repository.getBalance(ACCOUNT_NUMBER)).usingComparator(Amount::compareTo)
                .isEqualTo(ACCOUNT_BALANCE);
        assertThat(repository.getBalance(ANOTHER_ACCOUNT_NUMBER)).usingComparator(Amount::compareTo)
                .isEqualTo(ANOTHER_ACCOUNT_BALANCE);
    }

    @Test
    public void not_transfer_money_from_non_existing_account_to() {
        Integer nonExistingAccountNumber = 0;
        Amount transferAmount = new Amount(new BigDecimal("10"));

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> repository.transfer(nonExistingAccountNumber, ANOTHER_ACCOUNT_NUMBER, transferAmount));
    }

    @Test
    public void not_transfer_money_to_non_existing_account_to() {
        Integer nonExistingAccountNumber = 0;
        Amount transferAmount = new Amount(new BigDecimal("10"));

        assertThatExceptionOfType(AccountNotFoundException.class)
                .isThrownBy(() -> repository.transfer(ACCOUNT_NUMBER, nonExistingAccountNumber, transferAmount));
    }
}