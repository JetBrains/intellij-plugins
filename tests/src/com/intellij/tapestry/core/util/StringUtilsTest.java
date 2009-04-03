package com.intellij.tapestry.core.util;

import org.testng.annotations.Test;

public class StringUtilsTest {

    @Test
    public void constructor() {
        new StringUtils();
    }

    @Test
    public void capitalize_empty() {
        assert StringUtils.capitalize(null) == null;

        assert StringUtils.capitalize("").length() == 0;
    }

    @Test
    public void capitalize_with_content() {
        assert StringUtils.capitalize("a").equals("A");
        assert StringUtils.capitalize("A").equals("A");

        assert StringUtils.capitalize("abc").equals("Abc");
        assert StringUtils.capitalize("abC").equals("AbC");
        assert StringUtils.capitalize("Abc").equals("Abc");
    }

    @Test
    public void uncapitalize_empty() {
        assert StringUtils.uncapitalize(null) == null;

        assert StringUtils.uncapitalize("").length() == 0;
    }

    @Test
    public void uncapitalize_with_content() {
        assert StringUtils.uncapitalize("A").equals("a");
        assert StringUtils.uncapitalize("a").equals("a");

        assert StringUtils.uncapitalize("Abc").equals("abc");
        assert StringUtils.uncapitalize("AbC").equals("abC");
        assert StringUtils.uncapitalize("abc").equals("abc");
    }

    @Test
    public void isNotEmpty() {
        assert !StringUtils.isNotEmpty(null);

        assert !StringUtils.isNotEmpty("");

        assert StringUtils.isNotEmpty("$");

        assert StringUtils.isNotEmpty("abc");
    }

    @Test
    public void truncateWords() {
        assert StringUtils.truncateWords("hey you", 10).equals("hey you");

        assert StringUtils.truncateWords("hey you", 4).equals("hey");

        assert StringUtils.truncateWords("hey you", 6).equals("hey");

        assert StringUtils.truncateWords("hey hey you", 8).equals("hey hey");

        assert StringUtils.truncateWords("hey hey you", 9).equals("hey hey");
    }
}
