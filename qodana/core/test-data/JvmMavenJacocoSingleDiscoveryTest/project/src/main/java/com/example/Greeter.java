package com.example;

public class Greeter {

    public String greet(String name) {
        if (name == null || name.isEmpty()) {
            return "Hello, stranger!";
        }
        return "Hello, " + name + "!";
    }

    public String farewell(String name) {
        return "Goodbye, " + name + "!";
    }
}
