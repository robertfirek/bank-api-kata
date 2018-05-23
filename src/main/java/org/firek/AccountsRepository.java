package org.firek;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.firek.exceptions.AccountNotFoundException;
import org.firek.exceptions.NotEnoughMoneyOnAccountException;

class AccountsRepository {

    private final Map<Integer, Account> accounts;

    AccountsRepository(Account... accounts) {
        this.accounts = Arrays.stream(accounts)
                .collect(Collectors.toMap(Account::getNumber, Function.identity()));
    }

    Amount getBalance(Integer accountNumber) {
        Account currentAccount = accounts.get(accountNumber);
        return ofNullable(currentAccount)
                .map(Account::getBalance)
                .orElseThrow(AccountNotFoundException::new);
    }

    void transfer(Integer sourceAccountNumber, Integer targetAccountNumber, Amount transferAmount) {
        Amount sourceAccountBalance = getBalance(sourceAccountNumber);
        Amount targetAccountBalance = getBalance(targetAccountNumber);

        if (sourceAccountBalance.compareTo(transferAmount) < 0) {
            throw new NotEnoughMoneyOnAccountException();
        }

        Amount newBalanceForSourceAccount = sourceAccountBalance.subtract(transferAmount);
        Amount newBalanceForTargetAccount = targetAccountBalance.add(transferAmount);

        accounts.put(sourceAccountNumber, new Account(sourceAccountNumber, newBalanceForSourceAccount));
        accounts.put(targetAccountNumber, new Account(sourceAccountNumber, newBalanceForTargetAccount));
    }
}
