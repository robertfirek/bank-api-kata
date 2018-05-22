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

    private static final Integer ACCOUNT_NUMBER = 1;
    private static final Amount BALANCE_AMOUNT = new Amount(new BigDecimal("321.45"));
    private static final Account ACCOUNT = new Account(ACCOUNT_NUMBER, BALANCE_AMOUNT);

    private static final Integer ANOTHER_ACCOUNT_NUMBER = 2;
    private static final Amount ANOTHER_BALANCE_AMOUNT = new Amount(new BigDecimal("31.15"));
    private static final Account ANOTHER_ACCOUNT = new Account(ANOTHER_ACCOUNT_NUMBER, ANOTHER_BALANCE_AMOUNT);

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

    private Object[] account() {
        return new Object[] { ACCOUNT, BALANCE_AMOUNT };
    }

    private Object[] anotherAccount() {
        return new Object[] { ANOTHER_ACCOUNT, ANOTHER_BALANCE_AMOUNT };
    }

    private Amount amount(HttpResponse response) throws IOException {
        return new Gson().fromJson(EntityUtils.toString(response.getEntity()), Amount.class);
    }

    private int responseCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    private void awaitShutdown() {
        // Remove when https://github.com/perwendel/spark/issues/705 is fixed.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}