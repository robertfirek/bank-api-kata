package org.firek;

import static java.util.Optional.ofNullable;
import static spark.Spark.exception;
import static spark.Spark.get;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;

public class Bank {
    public static void main(String[] args) {
        new Bank().routes();
    }

    void routes(Account... accounts) {
        Map<Integer, Account> accountRepository = createAccountsRepository(accounts);

        get("/account/:accountNumber/balance", (request, response) -> {
            Integer accountNumber = new Integer(request.params("accountNumber"));
            return new Gson().toJson(getAccountBalance(accountNumber, accountRepository));
        });

        exception(AccountNotFoundException.class, (exception, request, response) -> {
            response.type("");
            response.body("");
            response.status(HttpStatus.SC_NOT_FOUND);
        });
    }

    private Amount getAccountBalance(Integer accountNumber, Map<Integer, Account> accountRepository) {
        Account currentAccount = accountRepository.get(accountNumber);
        return ofNullable(currentAccount)
                .map(Account::getBalance)
                .orElseThrow(AccountNotFoundException::new);
    }

    private Map<Integer, Account> createAccountsRepository(Account[] accounts) {
        return Arrays.stream(accounts)
                .collect(Collectors.toMap(Account::getNumber, Function.identity()));
    }

    private class AccountNotFoundException extends RuntimeException {
    }
}
