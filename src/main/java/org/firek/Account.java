package org.firek;

class Account {
    private final Integer accountNumber;
    private final Amount balanceAmount;

    Account(Integer accountNumber, Amount balanceAmount) {
        this.accountNumber = accountNumber;
        this.balanceAmount = balanceAmount;
    }

    Integer getNumber() {
        return accountNumber;
    }

    Amount getBalance() {
        return balanceAmount;
    }
}
