package {
class From {
    var v : Foo = new Foo(null);
}
}

import flash.events.KeyboardEvent;

class C1 {}

/**
 * this is Foo docs
 */
class Fo<caret>o {
    var v : Foo;

    public function Foo(p: Foo) {
        v = p;
        new Foo(this);
    }

    function foo(p: KeyboardEvent) : String {
        var v : Foo;
    }


}

function C2(): Foo {}
function C3(): Foo {}

