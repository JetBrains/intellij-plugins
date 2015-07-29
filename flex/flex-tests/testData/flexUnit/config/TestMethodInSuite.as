package com.foo {

[Suite]
public class TestMethodInSuite {
    <caret expected="Class com.foo.TestMethodInSuite">

    [Test]
    public function foo() {
            <caret expected="Method com.foo.TestMethodInSuite.foo()">
            }
    }

}

    <caret expected="Class com.foo.TestMethodInSuite">