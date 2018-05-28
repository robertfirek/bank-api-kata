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

import spark.Request;
import spark.Response;

public class Bank {

    public static void main(String... args) {
        Account[] accounts = new ArgumentsToAccountsConverter().convert(args);
        new Bank().routes(accounts);
    }

    private void routes(Account... accounts) {
        AccountsRepository accountRepository = new AccountsRepository(accounts);

        get("/account/:accountNumber/balance",
                (request, response) -> handleBalanceRequest(accountRepository, request));
        post("/account/:sourceAccountNumber/transfer/:targetAccountNumber",
                ContentType.APPLICATION_JSON.toString(),
                (request, response) -> handleTransferRequest(accountRepository, request, response));

        exception(AccountNotFoundException.class,
                (exception, request, response) -> setResponseHttpStatus(response, SC_NOT_FOUND));
        exception(NotEnoughMoneyOnAccountException.class,
                (exception, request, response) -> setResponseHttpStatus(response, SC_NO_CONTENT));
    }

    private String handleBalanceRequest(AccountsRepository accountRepository, Request request) {
        Integer accountNumber = new Integer(request.params("accountNumber"));
        Amount accountBalance = accountRepository.getBalance(accountNumber);

        return new Gson().toJson(accountBalance);
    }

    private String handleTransferRequest(AccountsRepository accountRepository, Request request, Response response) {
        Integer sourceAccountNumber = new Integer(request.params("sourceAccountNumber"));
        Integer targetAccountNumber = new Integer(request.params("targetAccountNumber"));
        Amount transferAmount = new Gson().fromJson(request.body(), Amount.class);

        accountRepository.transfer(sourceAccountNumber, targetAccountNumber, transferAmount);

        setResponseHttpStatus(response, SC_OK);
        return "";
    }

    private void setResponseHttpStatus(Response response, int scNoContent) {
        response.type("");
        response.body("");
        response.status(scNoContent);
    }
}
