package {

class From {}

}

import com.foo.Local1Base;

class Local1 extends Local1Base {

    function bar() {
        return foo();
    }
}