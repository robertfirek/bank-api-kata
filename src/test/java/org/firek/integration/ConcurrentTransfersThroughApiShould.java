package org.firek.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static spark.Spark.awaitInitialization;
import static spark.Spark.stop;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Condition;
import org.firek.Account;
import org.firek.Amount;
import org.firek.Bank;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.google.gson.Gson;

@RunWith(ConcurrentTestRunner.class)
public class ConcurrentTransfersThroughApiShould {

    private static final String ACCOUNT_BALANCE_ENDPOINT_URI = "http://localhost:4567/account/%d/balance";
    private static final String TRANSFER_ENDPOINT_URI = "http://localhost:4567/account/%d/transfer/%d";

    private static final Integer ACCOUNT_NUMBER = 101;
    private static final Amount ACCOUNT_BALANCE = new Amount(new BigDecimal("0.01"));
    private static final Account ACCOUNT = new Account(ACCOUNT_NUMBER, ACCOUNT_BALANCE);

    private static final Integer ANOTHER_ACCOUNT_NUMBER = 102;
    private static final Amount ANOTHER_ACCOUNT_BALANCE = new Amount(new BigDecimal("22.22"));
    private static final Account ANOTHER_ACCOUNT = new Account(ANOTHER_ACCOUNT_NUMBER, ANOTHER_ACCOUNT_BALANCE);

    private static final Amount TRANSFER_AMOUNT = ACCOUNT_BALANCE;

    private static final Condition<Amount> NOT_BELOW_ZERO = new Condition<>(
            ConcurrentTransfersThroughApiShould::amountIsNotBelowZero, "Amount is not below zero");

    @Before
    public void setUp() {

        Bank.main(accountAsString(ACCOUNT), accountAsString(ANOTHER_ACCOUNT));
        awaitInitialization();
    }

    @Test
    @ThreadCount(10)
    public void keep_accounts_balances_consistent() throws IOException {
        String transferEndpoint = String.format(TRANSFER_ENDPOINT_URI, ACCOUNT_NUMBER, ANOTHER_ACCOUNT_NUMBER);
        String transferAmountAsJson = new Gson().toJson(TRANSFER_AMOUNT);

        Request.Post(transferEndpoint)
                .bodyString(transferAmountAsJson, ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
    }

    @After
    public void check_if_account_are_consistent_after_concurrent_transfers() throws IOException {
        Amount expectedBalanceForTargetAccount = ANOTHER_ACCOUNT_BALANCE.add(ACCOUNT_BALANCE);
        String balanceEndpointForAccount = String.format(ACCOUNT_BALANCE_ENDPOINT_URI, ACCOUNT_NUMBER);
        String balanceEndpointForAnotherAccount = String.format(ACCOUNT_BALANCE_ENDPOINT_URI, ANOTHER_ACCOUNT_NUMBER);

        try {
            HttpResponse balanceForAccountResponse = Request.Get(balanceEndpointForAccount)
                    .execute()
                    .returnResponse();
            HttpResponse balanceForAnotherAccountResponse = Request.Get(balanceEndpointForAnotherAccount)
                    .execute()
                    .returnResponse();

            assertThat(amount(balanceForAccountResponse)).usingComparator(Amount::compareTo)
                    .is(NOT_BELOW_ZERO);
            assertThat(amount(balanceForAnotherAccountResponse)).usingComparator(Amount::compareTo)
                    .isEqualTo(expectedBalanceForTargetAccount);
        } finally {
            shutdownServer();
        }
    }

    private Amount amount(HttpResponse response) throws IOException {
        return new Gson().fromJson(EntityUtils.toString(response.getEntity()), Amount.class);
    }

    private static boolean amountIsNotBelowZero(Amount amount) {
        return amount.compareTo(new Amount(BigDecimal.ZERO)) >= 0;
    }

    private String accountAsString(Account account) {
        return account.getNumber() + "," + account.getBalance().getAmount().toString();
    }

    private void shutdownServer() {
        stop();

        // Remove when https://github.com/perwendel/spark/issues/705 is fixed.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
