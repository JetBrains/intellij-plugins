package com.foo {

public class FlexUnit4WithNonDefaultConstructor {
    <caret expected="null">

    public function FlexUnit4WithNonDefaultConstructor(p: String = "foo", p2: Number, ... p3) {
            <caret expected="null">
    }

    [Test]
    public function foo() {
            <caret expected="null">
    }

    }

<caret expected="null">
}

    <caret expected="null">