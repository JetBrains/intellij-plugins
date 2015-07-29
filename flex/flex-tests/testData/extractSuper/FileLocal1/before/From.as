package {

class From {}

}


class Local1 {
    var v;

    function foo()  {
        v = 0;
    }

    function bar() {
        return foo();
    }
}