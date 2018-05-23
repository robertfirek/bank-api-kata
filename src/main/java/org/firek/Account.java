package org.firek;

public class Account {
    private final Integer accountNumber;
    private final Amount balanceAmount;

    public Account(Integer accountNumber, Amount balanceAmount) {
        this.accountNumber = accountNumber;
        this.balanceAmount = balanceAmount;
    }

    public Integer getNumber() {
        return accountNumber;
    }

    public Amount getBalance() {
        return balanceAmount;
    }
}
