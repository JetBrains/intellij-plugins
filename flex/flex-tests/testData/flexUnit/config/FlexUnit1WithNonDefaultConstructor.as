package com.foo {
import flexunit.framework.TestCase;

public class FlexUnit1WithNonDefaultConstructor extends TestCase {
    <caret expected="null">

    public function FlexUnit1WithNonDefaultConstructor(p:Number, p2:Number = 0, ...p3) {

    }

    public function testFoo() {
            <caret expected="null">
    }

}
<caret expected="null">
}

    <caret expected="null">