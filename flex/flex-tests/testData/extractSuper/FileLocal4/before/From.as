package {
class From {
    var v:Local1;
}
}


function zz() {
}

class Local1 {
    var v:Local1;

    function foo() {
        v = new Local1();
    }
}