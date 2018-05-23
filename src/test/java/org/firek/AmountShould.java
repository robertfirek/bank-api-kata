package org.firek;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class AmountShould {

    @Test
    @Parameters(method = "firstBiggerThenSecond, secondBiggerThenSecond, bothEqual")
    public void compare_two_amounts(Amount thisAmount, Amount thatAmount, int expectedResultOfComparison) {
        assertThat(thisAmount.compareTo(thatAmount)).isEqualTo(expectedResultOfComparison);
    }

    @Test
    public void add_two_amounts() {
        Amount resultAmount = new Amount(new BigDecimal("13.29")).add(new Amount(new BigDecimal("9.21")));

        assertThat(resultAmount.getAmount()).isEqualTo(new BigDecimal("22.50"));
    }

    @Test
    public void subtract_two_amounts() {
        Amount resultAmount = new Amount(new BigDecimal("10.00")).subtract(new Amount(new BigDecimal("9.21")));

        assertThat(resultAmount.getAmount()).isEqualTo(new BigDecimal("0.79"));
    }

    private Object[] firstBiggerThenSecond() {
        Amount biggerAmount = new Amount(new BigDecimal("10.00"));
        Amount smallerAmount = new Amount(new BigDecimal("9.00"));
        return new Object[] { biggerAmount, smallerAmount, 1 };
    }

    private Object[] secondBiggerThenSecond() {
        Amount biggerAmount = new Amount(new BigDecimal("10.00"));
        Amount smallerAmount = new Amount(new BigDecimal("9.00"));
        return new Object[] { smallerAmount, biggerAmount, -1 };
    }

    private Object[] bothEqual() {
        Amount amount = new Amount(new BigDecimal("10.00"));
        return new Object[] { amount, amount, 0 };
    }
}