package com.foo {
import flexunit.framework.TestCase;

[RunWith("zzz.MyRunner")]
public class WithCustomRunner {
    <caret expected="Class com.foo.WithCustomRunner">

    public function foo() {
            <caret expected="Method com.foo.WithCustomRunner.foo()">
    }

    function nonPublic(): {
            <caret expected="Method com.foo.WithCustomRunner.nonPublic()">
    }

    public static function staticFunc() {
            <caret expected="Method com.foo.WithCustomRunner.staticFunc()">
    }

    public function withParam(p:Number) {
            <caret expected="Method com.foo.WithCustomRunner.withParam()">
    }

    public function nonVoid():String {
            <caret expected="Method com.foo.WithCustomRunner.nonVoid()">
    }

    [Test]
    public function bar() {
            <caret expected="Method com.foo.WithCustomRunner.bar()">
    }
<caret expected="Class com.foo.WithCustomRunner">
}

    <caret expected="Class com.foo.WithCustomRunner">