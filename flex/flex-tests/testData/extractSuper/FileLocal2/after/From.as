package {
import com.foo.ILocal1;

class From {

    var v : ILocal1;

    function doo() {
        var v : ILocal1 = new Local1("abc");
        v.foo();
    }

}

}

import com.foo.ILocal1;

class Local1 implements ILocal1 {
    var v;

    public function Local1(p:String) {
    }

    public function foo() {
        v = 0;
    }

    function bar() {
        return foo();
    }
}