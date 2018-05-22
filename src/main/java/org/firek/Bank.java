package org.firek;

import static spark.Spark.get;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class Bank {
    public static void main(String[] args) {
        new Bank().routes(null);
    }

    void routes(Account... accounts) {
        Map<Integer, Account> accountRepository = createAccountsRepository(accounts);

        get("/account/:accountNumber/balance", (request, response) -> {
            Integer accountNumber = new Integer(request.params("accountNumber"));
            return new Gson().toJson(getAccountBalance(accountNumber, accountRepository));
        });

    }

    private Amount getAccountBalance(Integer accountNumber, Map<Integer, Account> accountRepository) {
        return accountRepository.get(accountNumber).getBalance();
    }

    private Map<Integer, Account> createAccountsRepository(Account[] accounts) {
        return Arrays.stream(accounts)
                .collect(Collectors.toMap(Account::getNumber, account -> account));
    }
}
