package {
}

class From {
    function foo() {
        var v : From = new From();
        v.foo();
        
        var v2 : From = new From();
        v2.bar();
    }

    function bar() {

    }
}