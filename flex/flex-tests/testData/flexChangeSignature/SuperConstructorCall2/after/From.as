/**
 * Created by IntelliJ IDEA.
 * User: ksafonov
 * Date: 05.11.10
 * Time: 10:16
 * To change this template use File | Settings | File Templates.
 */
package {
public class A {
    public function A(p:String, b:Boolean) {
    }
}
}

package {
public class B extends A {

    public function B(p:String, b:Boolean) {
        super(p, b); // propagate
    }
}
}

package {
public class C extends B {

    public function C(p:String, b:Boolean) {
        super(p, b); // propagate
    }
}
}

package {
public class D extends C {

    public function D(p:String) {
        super(p, true);
    }
}
}