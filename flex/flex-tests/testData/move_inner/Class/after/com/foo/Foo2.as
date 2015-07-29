package com.foo {
import flash.events.KeyboardEvent;

/**
 * this is Foo docs
 */
public class Foo2 {
    var v:Foo2;

    public function Foo2(p:Foo2) {
        v = p;
        new Foo2(this);
    }

    function foo(p:KeyboardEvent):String {
        var v:Foo2;
    }


}
}
