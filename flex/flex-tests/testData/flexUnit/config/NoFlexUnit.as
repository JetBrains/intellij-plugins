package com.foo {
import flexunit.framework.TestCase;

public class NoFlexUnit extends TestCase {
    <caret expected="null">

    public function testFoo() {
            <caret expected="null">
    }

    [Test]
    public function bar() {
            <caret expected="null">
    }

<caret expected="null">
}

    <caret expected="null">