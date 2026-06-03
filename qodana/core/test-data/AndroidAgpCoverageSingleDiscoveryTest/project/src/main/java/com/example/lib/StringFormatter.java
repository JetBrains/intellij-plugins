package com.example.lib;

public class StringFormatter {

    public String trimToEmpty(String input) {
        return input == null ? "" : input.trim();
    }

    public String repeat(String input, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(input);
        }
        return sb.toString();
    }

    public String toSnakeCase(String input) {
        return input.replaceAll("\\s+", "_").toLowerCase();
    }

    public boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }
}
