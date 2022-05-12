package com.intellij.tapestry.core.model.presentation.valueresolvers;

import org.testng.annotations.Test;

public class AbstractValueResolverTest {

    @Test
    public void getPrefix_null() {
        assert AbstractValueResolver.getPrefix(null, null) == null;
    }

    @Test
    public void getPrefix_defined_prefix() {
        assert AbstractValueResolver.getPrefix("prefix:", "default").equals("prefix");

        assert AbstractValueResolver.getPrefix("prefix:value", "default").equals("prefix");
    }

    @Test
    public void getPrefix_no_defined_prefix() {
        assert AbstractValueResolver.getPrefix(":", "default") == null;

        assert AbstractValueResolver.getPrefix(":value", "default") == null;

        assert AbstractValueResolver.getPrefix("value", "default").equals("default");
    }

    @Test
    public void getCleanValue_defined_prefix() {
        assert AbstractValueResolver.getCleanValue("prefix:").isEmpty();

        assert AbstractValueResolver.getCleanValue("prefix: value ").equals("value");

        assert AbstractValueResolver.getCleanValue("${prefix: value }").equals("value");

        assert AbstractValueResolver.getCleanValue("${prefix: value ").equals("value");

        assert AbstractValueResolver.getPrefix("${prefix: value }", "default").equals("prefix");
    }

    @Test
    public void getCleanValue_no_defined_prefix() {
        assert AbstractValueResolver.getCleanValue(":") == null;

        assert AbstractValueResolver.getCleanValue(": value ") == null;

        assert AbstractValueResolver.getCleanValue(" value ").equals("value");

        assert AbstractValueResolver.getCleanValue("${ value }").equals("value");

        assert AbstractValueResolver.getCleanValue("${ value ").equals("value");
    }
}
