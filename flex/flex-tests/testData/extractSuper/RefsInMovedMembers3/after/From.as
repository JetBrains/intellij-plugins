package {
}

class From {
    function foo() {
        var v:From = new FromEx();
        v.foo();

        var v2:FromEx = new FromEx();
        v2.bar();
    }
}
class FromEx extends From {

    function bar() {

    }
}