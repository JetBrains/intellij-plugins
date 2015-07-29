package {

class From {

    var v : Local1;

    function doo() {
        var v : Local1 = new Local1("abc");
        v.foo();
    }

}

}


class Local1 {
    var v;

    public function Local1(p:String) {
    }

    function foo()  {
        v = 0;
    }

    function bar() {
        return foo();
    }
}