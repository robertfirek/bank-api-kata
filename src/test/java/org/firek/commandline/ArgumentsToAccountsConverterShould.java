package org.firek.commandline;

import static java.lang.String.format;

import java.math.BigDecimal;

import org.assertj.core.api.Assertions;
import org.firek.Account;
import org.firek.Amount;
import org.junit.Test;

public class ArgumentsToAccountsConverterShould {

    private static final Integer ACCOUNT_NUMBER = 1;
    private static final String ACCOUNT_BALANCE = "20.12";
    private static final String ACCOUNT_AS_STRING = format("%d,%s", ACCOUNT_NUMBER, ACCOUNT_BALANCE);

    private static final Integer ANOTHER_ACCOUNT_NUMBER = 2;
    private static final String ANOTHER_ACCOUNT_BALANCE = "30.12";
    private static final String ANOTHER_ACCOUNT_AS_STRING = format("%d,%s", ANOTHER_ACCOUNT_NUMBER, ANOTHER_ACCOUNT_BALANCE);

    private static final String[] ARGUMENTS = new String[] { ACCOUNT_AS_STRING, ANOTHER_ACCOUNT_AS_STRING };

    @Test
    public void convert_list_of_arguments_to_accounts() {
        Account[] accounts = new ArgumentsToAccountsConverter().convert(ARGUMENTS);

        Assertions.assertThat(accounts).usingElementComparator(this::accountComparator)
                .containsExactly(
                        account(ACCOUNT_NUMBER, ACCOUNT_BALANCE),
                        account(ANOTHER_ACCOUNT_NUMBER, ANOTHER_ACCOUNT_BALANCE)
                );
    }

    @Test
    public void not_return_accounts_when_accounts_are_not_defined() {
        Account[] accounts = new ArgumentsToAccountsConverter().convert(null);

        Assertions.assertThat(accounts).isEmpty();
    }

    private Account account(Integer accountNumber, String accountBalance) {
        return new Account(accountNumber, new Amount(new BigDecimal(accountBalance)));
    }

    private int accountComparator(Account thisAccount, Account thatAccount) {
        return accountNumberIsTheSame(thisAccount, thatAccount) && amountIsEqual(thisAccount, thatAccount) ? 0 : -1;
    }

    private boolean accountNumberIsTheSame(Account thisAccount, Account thatAccount) {
        return thisAccount.getNumber().equals(thatAccount.getNumber());
    }

    private boolean amountIsEqual(Account thisAccount, Account thatAccount) {
        return thisAccount.getBalance().compareTo(thatAccount.getBalance()) == 0;
    }
}