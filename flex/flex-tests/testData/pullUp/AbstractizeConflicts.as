package bar {
import foo.ISuper;
public class Sub implements ISuper {
    function foo(p:Aux1, q:Aux2):Aux3 {
    }
    function foo2(p:Aux1, q:Aux2) {
    }
    function foo3(p:*, q:UnexistingClass) {
    }
    function foo4(q:Aux1) {
    }
    function foo5() {
    }
}

class Aux1 {}
class Aux2 {}
class Aux3 {}
}

package foo {

    public interface ISuper {}
}