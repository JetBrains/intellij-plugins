package {
class From {
    var v:Local1;

    function zz() {
        v = new Local1();
        v.foo();

        var v2: Local1 = new Local1();
        v2.bar();
    }
}
}


function zz() {
}

class Local1 {
    function foo() {
    }
    function bar() {}
}