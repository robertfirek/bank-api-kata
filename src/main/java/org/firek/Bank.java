package org.firek;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;

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
        AccountsRepository accountRepository = new AccountsRepository(accounts);

        get("/account/:accountNumber/balance", (request, response) -> {
            Integer accountNumber = new Integer(request.params("accountNumber"));
            return new Gson().toJson(accountRepository.getBalance(accountNumber));
        });

        post("/account/:sourceAccountNumber/transfer/:targetAccountNumber",
                ContentType.APPLICATION_JSON.toString(),
                (request, response) -> {
                    Integer sourceAccountNumber = new Integer(request.params("sourceAccountNumber"));
                    Integer targetAccountNumber = new Integer(request.params("targetAccountNumber"));
                    Amount transferAmount = new Gson().fromJson(request.body(), Amount.class);

                    accountRepository.transfer(sourceAccountNumber, targetAccountNumber, transferAmount);

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
}
