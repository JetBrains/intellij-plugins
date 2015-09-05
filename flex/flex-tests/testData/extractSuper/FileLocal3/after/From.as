package {
class From {
    var v:Local1;

    function zz() {
        v = new Local1Ex();
        v.foo();

        var v2: Local1Ex = new Local1Ex();
        v2.bar();
    }
}
}


function zz() {
}

class Local1 {
    function foo() {
    }
}
class Local1Ex extends Local1 {
    function bar() {
    }
}