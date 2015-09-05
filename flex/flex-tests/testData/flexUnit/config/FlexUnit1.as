package com.foo {
import flexunit.framework.TestCase;

public class FlexUnit1 extends TestCase {
    <caret expected="Class com.foo.FlexUnit1">

    public function FlexUnit1(p: String = "foo", ... p2) {
    }

    public function testFoo() {
            <caret expected="Method com.foo.FlexUnit1.testFoo()">
    }

    public function testFoo2() : void {
            <caret expected="Method com.foo.FlexUnit1.testFoo2()">
    }

    public function bar() {
            <caret expected="Class com.foo.FlexUnit1">
    }

    public function testWithParam(p : String, p2 : String = "", ...p3) {
            <caret expected="Class com.foo.FlexUnit1">
    }

    public function testWithOptionalParam1(p : String = "") {
            <caret expected="Method com.foo.FlexUnit1.testWithOptionalParam1()">
    }

    public function testWithOptionalParam2(...p){
            <caret expected="Method com.foo.FlexUnit1.testWithOptionalParam2()">
    }

    public function testWithOptionalParam3(p:String = "", ...p2) {
            <caret expected="Method com.foo.FlexUnit1.testWithOptionalParam3()">
    }

    protected function testProtected() {
                <caret expected="Class com.foo.FlexUnit1">
    }

    internal function testInternal() {
                <caret expected="Class com.foo.FlexUnit1">
    }

    public static function testStatic() {
                <caret expected="Class com.foo.FlexUnit1">
    }

    public function testNonVoid() : Number{
                <caret expected="Method com.foo.FlexUnit1.testNonVoid()">
        return 0;
    }

    public static function get testGet() : Number {
        <caret expected="Class com.foo.FlexUnit1">
        return 0;
    }

    }

<caret expected="Class com.foo.FlexUnit1">
}

    <caret expected="Class com.foo.FlexUnit1">