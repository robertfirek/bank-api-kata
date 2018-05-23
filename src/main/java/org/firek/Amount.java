package org.firek;

import java.math.BigDecimal;

public class Amount {
    private final BigDecimal amount;

    public Amount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int compareTo(Amount amountToCompare) {
        return this.amount.compareTo(amountToCompare.amount);
    }

    public Amount subtract(Amount amountToSubtract) {
        return new Amount(this.amount.subtract(amountToSubtract.amount));
    }

    public Amount add(Amount amountToAdd) {
        return new Amount(this.amount.add(amountToAdd.amount));
    }
}
