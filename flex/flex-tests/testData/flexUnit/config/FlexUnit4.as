package com.foo {

public class FlexUnit4 {
    <caret expected="Class com.foo.FlexUnit4">

    public function FlexUnit4(p: String = "foo", ... p2) {
    }

    [Test]
    public function foo() {
            <caret expected="Method com.foo.FlexUnit4.foo()">
    }

    [Test]
    public function foo() : void {
            <caret expected="Method com.foo.FlexUnit4.foo()">
    }

    public function bar() {
            <caret expected="Class com.foo.FlexUnit4">
    }

    [Test]
    public function withParam(p : String, p2 : String = "", ...p3) {
            <caret expected="Class com.foo.FlexUnit4">
    }

    [Test]
    public function withOptionalParam1(p : String = "") {
            <caret expected="Method com.foo.FlexUnit4.withOptionalParam1()">
    }

    public function withOptionalParam1A(p : String = "") {
            <caret expected="Class com.foo.FlexUnit4">
    }

    [Test]
    [Ignore]
    public function testOptionalParam1B(p : String = "") {
            <caret expected="Class com.foo.FlexUnit4">
    }

    [Test]
    public function withOptionalParam2(...p){
            <caret expected="Method com.foo.FlexUnit4.withOptionalParam2()">
    }

    [Test]
    public function withOptionalParam3(p:String = "", ...p2) {
            <caret expected="Method com.foo.FlexUnit4.withOptionalParam3()">
    }

    [Test]
    protected function fooProtected() {
        <caret expected="Class com.foo.FlexUnit4">
    }

    [Test]
    internal function fooInternal() {
        <caret expected="Class com.foo.FlexUnit4">
    }

    [Test]
    public static function fooStatic() {
        <caret expected="Class com.foo.FlexUnit4">
    }

    [Test]
    public static function get foo() : Number {
        <caret expected="Class com.foo.FlexUnit4">
        return 0;
    }

    [Test]
    public static function nonVoid() : Number {
        <caret expected="Class com.foo.FlexUnit4">
    }

    }

<caret expected="Class com.foo.FlexUnit4">
}

    <caret expected="Class com.foo.FlexUnit4">