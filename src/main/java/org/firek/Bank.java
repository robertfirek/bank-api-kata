package org.firek;

import static java.util.Optional.ofNullable;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.entity.ContentType;
import org.firek.commandline.ArgumentsToAccountsConverter;
import org.firek.exceptions.AccountNotFoundException;
import org.firek.exceptions.NotEnoughMoneyOnAccountException;

import com.google.gson.Gson;

public class Bank {
    public static void main(String[] args) {
        Account[] accounts = new ArgumentsToAccountsConverter().convert(args);
        new Bank().routes(accounts);
    }

    void routes(Account... accounts) {
        Map<Integer, Account> accountRepository = createAccountsRepository(accounts);

        get("/account/:accountNumber/balance", (request, response) -> {
            Integer accountNumber = new Integer(request.params("accountNumber"));
            return new Gson().toJson(getAccountBalance(accountNumber, accountRepository));
        });

        post("/account/:sourceAccountNumber/transfer/:targetAccountNumber",
                ContentType.APPLICATION_JSON.toString(),
                (request, response) -> {
                    Integer sourceAccountNumber = new Integer(request.params("sourceAccountNumber"));
                    Integer targetAccountNumber = new Integer(request.params("targetAccountNumber"));
                    Amount transferAmount = new Gson().fromJson(request.body(), Amount.class);

                    transfer(sourceAccountNumber, targetAccountNumber, transferAmount, accountRepository);

                    response.type("");
                    response.body("");
                    response.status(SC_OK);
                    return "";
                });

        exception(AccountNotFoundException.class, (exception, request, response) -> {
            response.type("");
            response.body("");
            response.status(SC_NOT_FOUND);
        });

        exception(NotEnoughMoneyOnAccountException.class, (exception, request, response) -> {
            response.type("");
            response.body("");
            response.status(SC_NO_CONTENT);
        });
    }

    private void transfer(Integer sourceAccountNumber, Integer targetAccountNumber, Amount transferAmount,
            Map<Integer, Account> accountRepository) {
        Amount sourceAccountBalance = getAccountBalance(sourceAccountNumber, accountRepository);
        Amount targetAccountBalance = getAccountBalance(targetAccountNumber, accountRepository);

        if (sourceAccountBalance.compareTo(transferAmount) < 0) {
            throw new NotEnoughMoneyOnAccountException();
        }

        Amount newBalanceForSourceAccount = sourceAccountBalance.subtract(transferAmount);
        Amount newBalanceForTargetAccount = targetAccountBalance.add(transferAmount);

        accountRepository.put(sourceAccountNumber, new Account(sourceAccountNumber, newBalanceForSourceAccount));
        accountRepository.put(targetAccountNumber, new Account(sourceAccountNumber, newBalanceForTargetAccount));
    }

    private Amount getAccountBalance(Integer accountNumber, Map<Integer, Account> accountRepository) {
        Account currentAccount = accountRepository.get(accountNumber);
        return ofNullable(currentAccount)
                .map(Account::getBalance)
                .orElseThrow(AccountNotFoundException::new);
    }

    private Map<Integer, Account> createAccountsRepository(Account[] accounts) {
        return Arrays.stream(accounts).collect(Collectors.toMap(Account::getNumber, Function.identity()));
    }

}
