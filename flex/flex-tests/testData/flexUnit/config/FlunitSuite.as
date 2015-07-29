package com.foo {
import net.digitalprimates.fluint.tests.TestSuite;

public class FlunitSuite extends TestSuite {
    <caret expected="Class com.foo.FlunitSuite">

    public function foo() {
            <caret expected="Class com.foo.FlunitSuite">
    }

    function nonPublic(): {
            <caret expected="Class com.foo.FlunitSuite">
    }

    public static function staticFunc() {
            <caret expected="Class com.foo.FlunitSuite">
    }

    public function withParam(p:Number) {
            <caret expected="Class com.foo.FlunitSuite">
    }

    public function nonVoid():String {
            <caret expected="Class com.foo.FlunitSuite">
    }

    [Test]
    public function testFoo() {
            <caret expected="Method com.foo.FlunitSuite.testFoo()">
    }
<caret expected="Class com.foo.FlunitSuite">
}

    <caret expected="Class com.foo.FlunitSuite">