package {
class From {
    var v:Local1;
}
}


function zz() {
}

interface Local1 {
    function foo();
}
class Local1Ex implements Local1 {
    var v:Local1;

    public function foo() {
        v = new Local1Ex();
    }
}