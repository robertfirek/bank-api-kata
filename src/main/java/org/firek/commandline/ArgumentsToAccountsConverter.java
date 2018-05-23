package org.firek.commandline;

import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.util.Arrays;

import org.firek.Account;
import org.firek.Amount;

public class ArgumentsToAccountsConverter {

    public Account[] convert(String[] arguments) {
        return ofNullable(arguments)
                .map(this::convertArgumentsToAccounts)
                .orElseGet(() -> new Account[] {});
    }

    private Account[] convertArgumentsToAccounts(String[] arguments) {
        return Arrays.stream(arguments)
                .map(this::convertArgumentToAccount)
                .toArray(Account[]::new);
    }

    private Account convertArgumentToAccount(String argument) {
        String[] accountNumberAndAmount = argument.split(",");
        Integer accountNumber = Integer.valueOf(accountNumberAndAmount[0]);
        String accountBalanceAsString = accountNumberAndAmount[1];

        return new Account(accountNumber, new Amount(new BigDecimal(accountBalanceAsString)));
    }
}
