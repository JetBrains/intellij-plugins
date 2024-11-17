<?php

use PHPUnit\Framework\TestCase;
use qodana\FooCls;

class FooTest extends TestCase
{
    protected $foo;

    public function setUp(): void
    {
        $this->foo = new FooCls();
    }

    public function test()
    {
        $this->foo->foo();
        $this->foo->bar(1);
        $this->assertEquals(42, $this->foo->baz(1));
    }
}
