package com.foo {

public class FlexUnit4WithFlexUnit1 {
    <caret expected="null">

    public function FlexUnit4(p: String = "foo", ... p2) {
    }

    [Test]
    public function foo() {
            <caret expected="null">
    }

    public function bar() {
            <caret expected="null">
    }

    [Test]
    public function withParam(p : String, p2 : String = "", ...p3) {
            <caret expected="null">
    }

    [Test]
    public function withOptionalParam1(p : String = "") {
            <caret expected="null">
    }

    public function withOptionalParam1A(p : String = "") {
            <caret expected="null">
    }

    [Test]
    public function withOptionalParam2(...p){
            <caret expected="null">
    }

    [Test]
    public function testWithOptionalParam3(p:String = "", ...p2) {
            <caret expected="null">
    }

    }

<caret expected="null">
}

    <caret expected="null">