package org.firek;

import java.math.BigDecimal;

class Amount {
    private final BigDecimal amount;

    Amount(BigDecimal amount) {
        this.amount = amount;
    }

    BigDecimal getAmount() {
        return amount;
    }
}
