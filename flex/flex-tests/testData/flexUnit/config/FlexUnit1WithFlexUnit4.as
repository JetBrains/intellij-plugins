package com.foo {
import flexunit.framework.TestCase;

public class FlexUnit1WithFlexUnit4 extends TestCase {
    <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">

    public function FlexUnit1WithFlexUnit4(p: String = "foo", ... p2) {
    }

    public function testFoo() {
            <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testFoo()">
    }

    public function testFoo2() :void {
            <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testFoo2()">
    }

    [Ignore]
    public function testNotIgnored() {
            <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testNotIgnored()">
    }

    [Ignore]
    [Test]
    public function testNotIgnored2() {
            <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testNotIgnored2()">
    }

    public function bar() {
            <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">
    }

    public function testWithParam(p : String, p2 : String = "", ...p3) {
            <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">
    }

    public function testWithOptionalParam1(p : String = "") {
            <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testWithOptionalParam1()">
    }

    public function testWithOptionalParam2(...p){
            <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testWithOptionalParam2()">
    }

    public function testWithOptionalParam3(p:String = "", ...p2) {
            <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testWithOptionalParam3()">
    }

    protected function testProtected() {
                <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">
    }

    internal function testInternal() {
                <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">
    }

    public static function testStatic() {
                <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">
    }

    public function testNonVoid() : Number {
                <caret expected="Method com.foo.FlexUnit1WithFlexUnit4.testNonVoid()">
        return 0;
    }

    public static function get testGet() : Number {
        <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">
        return 0;
    }

    }

<caret expected="Class com.foo.FlexUnit1WithFlexUnit4">
}

    <caret expected="Class com.foo.FlexUnit1WithFlexUnit4">