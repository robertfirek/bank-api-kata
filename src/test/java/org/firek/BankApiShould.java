package org.firek;

import static org.assertj.core.api.Assertions.assertThat;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class BankApiShould {

    private static final String ACCOUNT_BALANCE_ENDPOINT_URI = "http://localhost:4567/account/%d/balance";
    private static final String TRANSFER_ENDPOINT_URI = "http://localhost:4567/account/%d/transfer/%d";

    private static final Integer ACCOUNT_NUMBER = 1;
    private static final Amount BALANCE_AMOUNT = new Amount(new BigDecimal("321.45"));
    private static final Account ACCOUNT = new Account(ACCOUNT_NUMBER, BALANCE_AMOUNT);

    private static final Integer ANOTHER_ACCOUNT_NUMBER = 2;
    private static final Amount ANOTHER_BALANCE_AMOUNT = new Amount(new BigDecimal("31.15"));
    private static final Account ANOTHER_ACCOUNT = new Account(ANOTHER_ACCOUNT_NUMBER, ANOTHER_BALANCE_AMOUNT);

    private static final Amount TRANSFER_AMOUNT = new Amount(new BigDecimal("30.15"));

    @Before
    public void setUp() {
        Bank bank = new Bank();
        bank.routes(ACCOUNT, ANOTHER_ACCOUNT);

        awaitInitialization();
    }

    @After
    public void tearDown() {
        stop();
        awaitShutdown();
    }

    @Test
    @Parameters(method = "account, anotherAccount")
    public void return_balance_for_given_balance(Account account, Amount expectedBalance) throws Exception {
        String transferEndpoint = String.format(ACCOUNT_BALANCE_ENDPOINT_URI, account.getNumber());

        HttpResponse response = Request.Get(transferEndpoint).execute().returnResponse();

        assertThat(responseCode(response)).isEqualTo(HttpStatus.SC_OK);
        assertThat(amount(response)).usingComparator(Comparator.comparing(Amount::getAmount)).isEqualTo(expectedBalance);
    }

    @Test
    public void inform_that_account_cannot_be_found() throws Exception {
        Integer notExistingAccountNumber = 101;
        String balanceEndpoint = String.format(ACCOUNT_BALANCE_ENDPOINT_URI, notExistingAccountNumber);

        HttpResponse response = Request.Get(balanceEndpoint).execute().returnResponse();

        assertThat(responseCode(response)).isEqualTo(HttpStatus.SC_NOT_FOUND);
        assertThat(responseBody(response)).isBlank();
    }

    @Test
    public void transfer_money_from_source_account_to_target_account() throws Exception {
        String transferEndpoint = String.format(TRANSFER_ENDPOINT_URI, ACCOUNT_NUMBER, ANOTHER_ACCOUNT_NUMBER);
        String balanceEndpointForAccount = String.format(ACCOUNT_BALANCE_ENDPOINT_URI, ACCOUNT_NUMBER);
        String balanceEndpointForAnotherAccount = String.format(ACCOUNT_BALANCE_ENDPOINT_URI, ANOTHER_ACCOUNT_NUMBER);
        String transferAmountAsJson = new Gson().toJson(TRANSFER_AMOUNT);
        Amount expectedBalanceForAccount = new Amount(BALANCE_AMOUNT.getAmount().subtract(TRANSFER_AMOUNT.getAmount()));
        Amount expectedBalanceForAnotherAccount = new Amount(
                ANOTHER_BALANCE_AMOUNT.getAmount().add(TRANSFER_AMOUNT.getAmount()));

        HttpResponse transferEndpointResponse = Request.Post(transferEndpoint)
                .bodyString(transferAmountAsJson, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        HttpResponse balanceForAccountResponse = Request.Get(balanceEndpointForAccount)
                .execute()
                .returnResponse();
        HttpResponse balanceForAnotherAccountResponse = Request.Get(balanceEndpointForAnotherAccount)
                .execute()
                .returnResponse();

        assertThat(transferEndpointResponse.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(responseCode(balanceForAccountResponse)).isEqualTo(HttpStatus.SC_OK);
        assertThat(amount(balanceForAccountResponse)).usingComparator(Comparator.comparing(Amount::getAmount))
                .isEqualTo(expectedBalanceForAccount);
        assertThat(amount(balanceForAnotherAccountResponse)).usingComparator(Comparator.comparing(Amount::getAmount))
                .isEqualTo(expectedBalanceForAnotherAccount);
    }

    private Object[] account() {
        return new Object[] { ACCOUNT, BALANCE_AMOUNT };
    }

    private Object[] anotherAccount() {
        return new Object[] { ANOTHER_ACCOUNT, ANOTHER_BALANCE_AMOUNT };
    }

    private Amount amount(HttpResponse response) throws IOException {
        return new Gson().fromJson(EntityUtils.toString(response.getEntity()), Amount.class);
    }

    private String responseBody(HttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    private int responseCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    private void awaitShutdown() {
        // Remove when https://github.com/perwendel/spark/issues/705 is fixed.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}