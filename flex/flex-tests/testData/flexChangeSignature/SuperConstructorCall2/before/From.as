/**
 * Created by IntelliJ IDEA.
 * User: ksafonov
 * Date: 05.11.10
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
package {
public class A {
    public function A<caret>(p:String) {
    }
}
}

package {
public class B extends A {

    public function B(p:String) {
        super(p); // propagate
    }
}
}

package {
public class C extends B {

    public function C(p:String) {
        super(p); // propagate
    }
}
}

package {
public class D extends C {

    public function D(p:String) {
        super(p);
    }
}
}