package com.intellij.tapestry.tests.core.util;

import com.intellij.tapestry.intellij.util.Validators;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class ValidatorsTest {

    @BeforeClass
    public void defaultConstructor() {
        new Validators();
    }

    @Test
    public void isValidPackageName_empty() {
        Validators.isValidPackageName(null);
        assert !Validators.isValidPackageName("");
    }

    @Test
    public void isValidPackageName_not_valid() {
        assert !Validators.isValidPackageName("1");

        assert !Validators.isValidPackageName("1abc");

        assert !Validators.isValidPackageName("a&b");

        assert !Validators.isValidPackageName("a..b");

        assert !Validators.isValidPackageName(".a");
    }

    @Test
    public void isValidPackageName_valid() {
        assert Validators.isValidPackageName("a");

        assert Validators.isValidPackageName("a.b");

        assert Validators.isValidPackageName("a1.b2");

        assert Validators.isValidPackageName("A");
    }

    @Test
    public void isValidComponentName_empty() {
        Validators.isValidComponentName(null);
        assert !Validators.isValidComponentName("");
    }

    @Test
    public void isValidComponentName_not_valid() {
        assert !Validators.isValidComponentName("1");

        assert !Validators.isValidComponentName("a.b");

        assert !Validators.isValidComponentName("a\\b");
    }

    @Test
    public void isValidComponentName_valid() {
        assert Validators.isValidComponentName("a");

        assert Validators.isValidComponentName("a/b");
    }
}
