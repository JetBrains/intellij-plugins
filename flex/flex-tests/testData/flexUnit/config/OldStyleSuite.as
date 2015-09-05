package com.foo {
import flexunit.framework.TestSuite;

public class OldStyleSuite extends TestSuite {
    <caret expected="Class com.foo.OldStyleSuite">

    public function foo() {
            <caret expected="Class com.foo.OldStyleSuite">
    }

    function nonPublic(): {
            <caret expected="Class com.foo.OldStyleSuite">
    }

    public static function staticFunc() {
            <caret expected="Class com.foo.OldStyleSuite">
    }

    public function withParam(p:Number) {
            <caret expected="Class com.foo.OldStyleSuite">
    }

    public function nonVoid():String {
            <caret expected="Class com.foo.OldStyleSuite">
    }

    [Test]
    public function testFoo() {
            <caret expected="Method com.foo.OldStyleSuite.testFoo()">
    }
<caret expected="Class com.foo.OldStyleSuite">
}

    <caret expected="Class com.foo.OldStyleSuite">