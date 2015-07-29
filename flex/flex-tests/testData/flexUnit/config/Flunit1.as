package com.foo {
import net.digitalprimates.fluint.tests.TestCase;

public class Flunit1 extends TestCase {
    <caret expected="Class com.foo.Flunit1">

    public function Flunit1(p: String = "foo", ... p2) {
    }

    public function testFoo() {
            <caret expected="Method com.foo.Flunit1.testFoo()">
    }

    public function testFoo2() : void {
            <caret expected="Method com.foo.Flunit1.testFoo2()">
    }

    public function bar() {
            <caret expected="Class com.foo.Flunit1">
    }

    public function testWithParam(p : String, p2 : String = "", ...p3) {
            <caret expected="Class com.foo.Flunit1">
    }

    public function testWithOptionalParam1(p : String = "") {
            <caret expected="Method com.foo.Flunit1.testWithOptionalParam1()">
    }

    public function testWithOptionalParam2(...p){
            <caret expected="Method com.foo.Flunit1.testWithOptionalParam2()">
    }

    public function testWithOptionalParam3(p:String = "", ...p2) {
            <caret expected="Method com.foo.Flunit1.testWithOptionalParam3()">
    }

    protected function testProtected() {
                <caret expected="Class com.foo.Flunit1">
    }

    internal function testInternal() {
                <caret expected="Class com.foo.Flunit1">
    }

    public static function testStatic() {
                <caret expected="Class com.foo.Flunit1">
    }

    public function testNonVoid() : Number{
                <caret expected="Method com.foo.Flunit1.testNonVoid()">
        return 0;
    }

    public static function get testGet() : Number {
        <caret expected="Class com.foo.Flunit1">
        return 0;
    }

    [Test]
    public function testFoo():void {
            <caret expected="Method com.foo.Flunit1.testFoo()">
    }
    }

<caret expected="Class com.foo.Flunit1">
}

    <caret expected="Class com.foo.Flunit1">