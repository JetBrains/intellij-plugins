package com.foo {

import flexunit.framework.TestCase;

public class FlexUnit4WithFlexUnit1_2 extends TestCase {
    <caret expected="Class com.foo.FlexUnit4WithFlexUnit1_2">

    [Test]
    [Ignore]
    public function testFoo() {
            <caret expected="Method com.foo.FlexUnit4WithFlexUnit1_2.testFoo()">
    }

    [Test]
    public function bar() {
            <caret expected="Class com.foo.FlexUnit4WithFlexUnit1_2">
    }

    }

<caret expected="Class com.foo.FlexUnit4WithFlexUnit1_2">
}

    <caret expected="Class com.foo.FlexUnit4WithFlexUnit1_2">