package com.avokin;

import cucumber.api.PendingException;
import io.cucumber.java8.En;

import static org.junit.Assert.fail;

public class MyStepDefs8 implements En {
    public MyStepDefs8() {
        Given("^normal step lambda$", () -> {
        });

        Given("^failing step lambda$", () -> {
            fail();
        });

        Given("^pending step lambda$", () -> {
            throw new PendingException();
        });
    }
}
