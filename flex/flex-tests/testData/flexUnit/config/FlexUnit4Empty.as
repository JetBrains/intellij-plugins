package com.foo {

public class FlexUnit4Empty {
    <caret expected="null">

    [Test]
    public function withParam(p) {

    }

    [Test]
    [Ignore]
    public function ignored()

    }

    [Ignore]
    public function testIgnored()  {

    }

    [Test]
    protected function protectedTest() {}

    [Test]
    private function privateTest() {}

    [Test]
    public static function staticTest() {}


<caret expected="null">
}

    <caret expected="null">